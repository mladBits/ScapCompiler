package com.touchstone.compiler.variables;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.index.XccdfIndex;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalConstantVariable;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalExternalVariable;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfValue;
import com.touchstone.compiler.model.resolved.xccdf.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class OvalVariableBindingResolverTest {

    private final OvalVariableBindingResolver resolver = new OvalVariableBindingResolver();

    @Test
    void resolve_shouldBindConstantVariables() {
        /*
         * OVAL content shape:
         *
         * <variables>
         *   <constant_variable id="oval:const:1" datatype="string">
         *     <value>Alpha</value>
         *     <value>Beta</value>
         *   </constant_variable>
         * </variables>
         *
         * No selected XCCDF rule/export is required for constant variables.
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedOvalConstantVariable constantVariable = constantVariable("oval:const:1", "Alpha", "Beta");
        ovalIndex.getVariableById().put(constantVariable.getId(), constantVariable);

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                List.of(),
                xccdfIndex,
                ovalIndex,
                Map.of()
        );

        VariableBinding binding = result.getBindingsById().get("oval:const:1");
        assertNotNull(binding);
        assertEquals("oval:const:1", binding.getVariableId());
        assertEquals(List.of("Alpha", "Beta"), binding.getValues());
        assertEquals(VariableBindingSource.OVAL_CONSTANT, binding.getSource());
        assertTrue(result.getUnresolvedVariableIds().isEmpty());
    }

    @Test
    void resolve_shouldPreferUserSuppliedValueOverXccdfDefault() {
        /*
         * XCCDF content shape:
         *
         * <Value id="xccdf_org.cisecurity_value_min_pass_len">
         *   <value>14</value>
         * </Value>
         *
         * <Rule id="rule-1">
         *   <check system="..." >
         *     <check-export export-name="oval:cis:var:1"
         *                   value-id="xccdf_org.cisecurity_value_min_pass_len"/>
         *     <check-content-ref href="oval.xml" name="oval:cis:def:1"/>
         *   </check>
         * </Rule>
         *
         * OVAL content shape:
         *
         * <external_variable id="oval:cis:var:1" datatype="int"/>
         *
         * Request overrides the XCCDF default:
         * variables["oval:cis:var:1"] = ["16"]
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedXccdfValue xccdfValue = xccdfValue("xccdf_org.cisecurity_value_min_pass_len", "14");
        xccdfIndex.getValuesById().put(xccdfValue.getId(), xccdfValue);

        ParsedOvalExternalVariable externalVariable = externalVariable("oval:cis:var:1");
        ovalIndex.getVariableById().put(externalVariable.getId(), externalVariable);

        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        List<ResolvedRuleOvalRefs> ruleOvalRefs = List.of(
                ruleRefs("rule-1",
                        referenceWithExports(export("oval:cis:var:1", "xccdf_org.cisecurity_value_min_pass_len")))
        );

        Map<String, List<String>> requestVariables = new LinkedHashMap<>();
        requestVariables.put("oval:cis:var:1", List.of("16"));

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                ruleOvalRefs,
                xccdfIndex,
                ovalIndex,
                requestVariables
        );

        VariableBinding binding = result.getBindingsById().get("oval:cis:var:1");
        assertNotNull(binding);
        assertEquals(List.of("16"), binding.getValues());
        assertEquals(VariableBindingSource.USER_SUPPLIED, binding.getSource());
        assertTrue(result.getUnresolvedVariableIds().isEmpty());
    }

    @Test
    void resolve_shouldFallbackToXccdfDefaultWhenNoUserSuppliedValueExists() {
        /*
         * Same export mapping as the previous test, but no request override.
         *
         * Expected binding:
         *   oval:cis:var:1 -> ["14"] from XCCDF_DEFAULT
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedXccdfValue xccdfValue = xccdfValue("xccdf_org.cisecurity_value_min_pass_len", "14");
        xccdfIndex.getValuesById().put(xccdfValue.getId(), xccdfValue);

        ParsedOvalExternalVariable externalVariable = externalVariable("oval:cis:var:1");
        ovalIndex.getVariableById().put(externalVariable.getId(), externalVariable);

        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        List<ResolvedRuleOvalRefs> ruleOvalRefs = List.of(
                ruleRefs("rule-1",
                        referenceWithExports(export("oval:cis:var:1", "xccdf_org.cisecurity_value_min_pass_len")))
        );

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                ruleOvalRefs,
                xccdfIndex,
                ovalIndex,
                Map.of()
        );

        VariableBinding binding = result.getBindingsById().get("oval:cis:var:1");
        assertNotNull(binding);
        assertEquals(List.of("14"), binding.getValues());
        assertEquals(VariableBindingSource.XCCDF_DEFAULT, binding.getSource());
        assertTrue(result.getUnresolvedVariableIds().isEmpty());
    }

    @Test
    void resolve_shouldIgnoreExportWhenOvalVariableDoesNotExist() {
        /*
         * XCCDF exports a variable ID that does not exist in parsed/indexed OVAL variables.
         *
         * <check-export export-name="oval:missing:var:1"
         *               value-id="xccdf_value_1"/>
         *
         * That export should be ignored. Nothing should be bound or marked unresolved
         * because the OVAL index has no such variable.
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedXccdfValue xccdfValue = xccdfValue("xccdf_value_1", "abc");
        xccdfIndex.getValuesById().put(xccdfValue.getId(), xccdfValue);

        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        List<ResolvedRuleOvalRefs> ruleOvalRefs = List.of(
                ruleRefs("rule-1",
                        referenceWithExports(export("oval:missing:var:1", "xccdf_value_1")))
        );

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                ruleOvalRefs,
                xccdfIndex,
                ovalIndex,
                Map.of()
        );

        assertTrue(result.getBindingsById().isEmpty());
        assertTrue(result.getUnresolvedVariableIds().isEmpty());
    }

    @Test
    void resolve_shouldNotOverwriteConstantVariableWhenExportPointsAtNonExternalVariable() {
        /*
         * OVAL content:
         *
         * <constant_variable id="oval:const:1">
         *   <value>CONST</value>
         * </constant_variable>
         *
         * XCCDF mistakenly exports to the same variable id:
         *
         * <check-export export-name="oval:const:1" value-id="xccdf_value_1"/>
         *
         * Since the target OVAL variable is not external_variable, export binding should be ignored.
         * The constant binding should remain.
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedXccdfValue xccdfValue = xccdfValue("xccdf_value_1", "override");
        xccdfIndex.getValuesById().put(xccdfValue.getId(), xccdfValue);

        ParsedOvalConstantVariable constantVariable = constantVariable("oval:const:1", "CONST");
        ovalIndex.getVariableById().put(constantVariable.getId(), constantVariable);

        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        List<ResolvedRuleOvalRefs> ruleOvalRefs = List.of(
                ruleRefs("rule-1",
                        referenceWithExports(export("oval:const:1", "xccdf_value_1")))
        );

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                ruleOvalRefs,
                xccdfIndex,
                ovalIndex,
                Map.of()
        );

        VariableBinding binding = result.getBindingsById().get("oval:const:1");
        assertNotNull(binding);
        assertEquals(List.of("CONST"), binding.getValues());
        assertEquals(VariableBindingSource.OVAL_CONSTANT, binding.getSource());
    }

    @Test
    void resolve_shouldTrackUnresolvedExternalVariables() {
        /*
         * OVAL content:
         *
         * <external_variable id="oval:cis:var:1"/>
         * <external_variable id="oval:cis:var:2"/>
         *
         * Only var:1 is bound through XCCDF export/default.
         * var:2 should be reported as unresolved.
         */
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        XccdfIndex xccdfIndex = new XccdfIndex();
        OvalIndex ovalIndex = new OvalIndex();

        ParsedXccdfValue xccdfValue = xccdfValue("xccdf_value_1", "enabled");
        xccdfIndex.getValuesById().put(xccdfValue.getId(), xccdfValue);

        ParsedOvalExternalVariable variable1 = externalVariable("oval:cis:var:1");
        ParsedOvalExternalVariable variable2 = externalVariable("oval:cis:var:2");
        ovalIndex.getVariableById().put(variable1.getId(), variable1);
        ovalIndex.getVariableById().put(variable2.getId(), variable2);

        ResolvedProfile resolvedProfile = profile(rule("rule-1"));
        List<ResolvedRuleOvalRefs> ruleOvalRefs = List.of(
                ruleRefs("rule-1",
                        referenceWithExports(export("oval:cis:var:1", "xccdf_value_1")))
        );

        ResolvedVariableBindings result = resolver.resolve(
                benchmark,
                resolvedProfile,
                ruleOvalRefs,
                xccdfIndex,
                ovalIndex,
                Map.of()
        );

        VariableBinding binding = result.getBindingsById().get("oval:cis:var:1");
        assertNotNull(binding);
        assertEquals(List.of("enabled"), binding.getValues());
        assertEquals(VariableBindingSource.XCCDF_DEFAULT, binding.getSource());

        assertEquals(List.of("oval:cis:var:2"), result.getUnresolvedVariableIds());
    }

    private ParsedOvalExternalVariable externalVariable(String id) {
        ParsedOvalExternalVariable variable = new ParsedOvalExternalVariable();
        variable.setId(id);
        variable.setDatatype("string");
        return variable;
    }

    private ParsedOvalConstantVariable constantVariable(String id, String... values) {
        ParsedOvalConstantVariable variable = new ParsedOvalConstantVariable();
        variable.setId(id);
        variable.setDatatype("string");
        variable.getValues().addAll(List.of(values));
        return variable;
    }

    private ParsedXccdfValue xccdfValue(String valueId, String defaultValue) {
        ParsedXccdfValue value = new ParsedXccdfValue();
        value.setId(valueId);
        value.setDefaultValue(defaultValue);
        value.setType("string");
        return value;
    }

    private ResolveCheckExport export(String exportName, String valueId) {
        ResolveCheckExport export = new ResolveCheckExport();
        export.setExportName(exportName);
        export.setValueId(valueId);
        return export;
    }

    private ResolvedCheckReference referenceWithExports(ResolveCheckExport... exports) {
        ResolvedCheckReference reference = new ResolvedCheckReference();
        reference.setSystem("http://oval.mitre.org/XMLSchema/oval-definitions-5");
        reference.setHref("oval.xml");
        reference.setName("oval:def:1");
        reference.getCheckExports().addAll(List.of(exports));
        return reference;
    }

    private ResolvedRuleOvalRefs ruleRefs(String ruleId, ResolvedCheckReference... references) {
        return new ResolvedRuleOvalRefs(ruleId, new ArrayList<>(List.of(references)));
    }

    private ResolvedXccdfRule rule(String ruleId) {
        ResolvedXccdfRule rule = new ResolvedXccdfRule();
        rule.setRuleId(ruleId);
        rule.setTitle("Rule " + ruleId);
        return rule;
    }

    private ResolvedProfile profile(ResolvedXccdfRule... rules) {
        ResolvedProfile profile = new ResolvedProfile();
        profile.setBenchmarkId("benchmark-1");
        profile.setProfileId("profile-1");
        profile.getSelectedRules().addAll(List.of(rules));
        return profile;
    }
}