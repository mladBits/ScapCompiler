package com.example.scap.variables;

import com.example.scap.index.OvalIndex;
import com.example.scap.index.XccdfIndex;
import com.example.scap.model.parsed.oval.variables.ParsedOvalConstantVariable;
import com.example.scap.model.parsed.oval.variables.ParsedOvalExternalVariable;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariable;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfValue;
import com.example.scap.model.resolved.xccdf.ResolveCheckExport;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OvalVariableBindingResolver {

    public ResolvedVariableBindings resolve(
            final ParsedXccdfBenchmark benchmark,
            final ResolvedProfile resolvedProfile,
            final List<ResolvedRuleOvalRefs> ruleOvalRefs,
            final XccdfIndex xccdfBenchmarkIndex,
            final OvalIndex ovalIndex,
            final Map<String, List<String>> requestVariables) {
        final ResolvedVariableBindings bindings = new ResolvedVariableBindings();
        final Map<String, ParsedXccdfValue> xccdfValuesById = xccdfBenchmarkIndex.getValuesById();
        final Map<String, List<ResolveCheckExport>> exportsByRuleId = indexCheckExportsByRule(ruleOvalRefs);
        final Map<String, List<String>> safeRequestVariables = requestVariables == null ? Map.of() : requestVariables;

        bindConstantVariables(ovalIndex, bindings);

        for (final ResolvedXccdfRule rule : resolvedProfile.getSelectedRules()) {
            final List<ResolveCheckExport> exports =
                    exportsByRuleId.getOrDefault(rule.getRuleId(), List.of());

            for (final ResolveCheckExport export : exports) {
                resolveExportBinding(
                        export,
                        xccdfValuesById,
                        ovalIndex,
                        safeRequestVariables,
                        bindings
                );
            }
        }

        markUnresolvedExternalVariables(ovalIndex, bindings);
        return bindings;
    }

    private Map<String, List<ResolveCheckExport>> indexCheckExportsByRule(final List<ResolvedRuleOvalRefs> ruleOvalRefs) {
        final Map<String, List<ResolveCheckExport>> exportsByRuleId = new LinkedHashMap<>();

        for (final ResolvedRuleOvalRefs rule : ruleOvalRefs) {
            final List<ResolveCheckExport> exports = rule.getReferences().stream()
                    .flatMap(reference -> reference.getCheckExports().stream())
                    .toList();

            exportsByRuleId.put(rule.getRuleId(), exports);
        }

        return exportsByRuleId;
    }

    private void bindConstantVariables(
            final OvalIndex ovalIndex,
            final ResolvedVariableBindings bindings
    ) {
        ovalIndex.getVariableById().values().stream()
                .filter(variable -> variable instanceof ParsedOvalConstantVariable)
                .map(variable -> (ParsedOvalConstantVariable) variable)
                .forEach(constantVariable -> {
                    final VariableBinding binding = new VariableBinding(
                            constantVariable.getId(),
                            new ArrayList<>(constantVariable.getValues()),
                            VariableBindingSource.OVAL_CONSTANT
                    );

                    bindings.getBindingsById().put(binding.getVariableId(), binding);
                });
    }

    private void resolveExportBinding(
            final ResolveCheckExport export,
            final Map<String, ParsedXccdfValue> xccdfValuesById,
            final OvalIndex ovalIndex,
            final Map<String, List<String>> requestVariables,
            final ResolvedVariableBindings bindings
    ) {
        final String ovalVariableId = export.getExportName();
        final String xccdfValueId = export.getValueId();

        final ParsedOvalVariable ovalVariable = ovalIndex.getVariableById().get(ovalVariableId);
        if (ovalVariable == null) {
            return;
        }

        if (!(ovalVariable instanceof ParsedOvalExternalVariable)) {
            return;
        }

        final List<String> requestValues = requestVariables.get(ovalVariableId);
        if (requestValues != null && !requestValues.isEmpty()) {
            final List<String> sanitizedRequestValues = requestValues.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .toList();

            if (!sanitizedRequestValues.isEmpty()) {
                final VariableBinding binding = new VariableBinding(
                        ovalVariableId,
                        new ArrayList<>(sanitizedRequestValues),
                        VariableBindingSource.USER_SUPPLIED
                );

                bindings.getBindingsById().put(ovalVariableId, binding);
                return;
            }
        }

        final ParsedXccdfValue xccdfValue = xccdfValuesById.get(xccdfValueId);
        final VariableBinding variableBinding = resolveXccdfValue(ovalVariableId, xccdfValue);

        if (variableBinding != null) {
            bindings.getBindingsById().put(ovalVariableId, variableBinding);
        }
    }

    private VariableBinding resolveXccdfValue(
            final String ovalVariableId,
            final ParsedXccdfValue xccdfValue) {
        if (xccdfValue == null) {
            return null;
        }

        final VariableBinding binding = new VariableBinding();
        binding.setVariableId(ovalVariableId);

        if (xccdfValue.getValue() != null && !xccdfValue.getValue().isBlank()) {
            binding.setValues(List.of(xccdfValue.getValue()));
            binding.setSource(VariableBindingSource.XCCDF_VALUE);
            return binding;
        }

        if (xccdfValue.getDefaultValue() != null && !xccdfValue.getDefaultValue().isBlank()) {
            binding.setValues(List.of(xccdfValue.getDefaultValue()));
            binding.setSource(VariableBindingSource.XCCDF_DEFAULT);
            return binding;
        }

        return null;
    }

    private void markUnresolvedExternalVariables(
            final OvalIndex ovalIndex,
            final ResolvedVariableBindings bindings
    ) {
        ovalIndex.getVariableById().values().stream()
                .filter(variable -> variable instanceof ParsedOvalExternalVariable)
                .map(ParsedOvalVariable::getId)
                .filter(variableId -> !bindings.getBindingsById().containsKey(variableId))
                .forEach(bindings.getUnresolvedVariableIds()::add);
    }
}
