package com.example.scap.service;

import com.example.scap.api.dto.CompileTemplateRequest;
import com.example.scap.api.dto.CompileTemplateResponse;
import com.example.scap.content.ContentPackageLoader;
import com.example.scap.index.OvalIndex;
import com.example.scap.index.OvalIndexBuilder;
import com.example.scap.index.XccdfIndex;
import com.example.scap.index.XccdfIndexBuilder;
import com.example.scap.model.compiled.CompiledTemplateRule;
import com.example.scap.model.compiled.ExecutionTemplate;
import com.example.scap.model.compiled.variables.CompiledLocalVariableExpression;
import com.example.scap.model.compiled.variables.CompiledVariable;
import com.example.scap.model.compiled.variables.CompiledVariableKind;
import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.compiled.variables.LocalVariablePlanCompiler;
import com.example.scap.model.parsed.oval.ParsedOval;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariable;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.model.resolved.oval.ResolvedOvalVariableClosure;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import com.example.scap.oval.OvalCheckCompilationResult;
import com.example.scap.oval.OvalCheckCompilationService;
import com.example.scap.oval.definition.CompiledOvalDefinitionPlan;
import com.example.scap.oval.definition.OvalDefinitionPlanCompiler;
import com.example.scap.parser.OvalParser;
import com.example.scap.parser.XccdfParser;
import com.example.scap.resolve.oval.OvalEvaluationSliceResolver;
import com.example.scap.resolve.oval.OvalVariableClosureResolver;
import com.example.scap.resolve.oval.ReferencedOvalDefinitionResolver;
import com.example.scap.resolve.xccdf.ProfileResolver;
import com.example.scap.resolve.xccdf.RuleOvalReferenceResolver;
import com.example.scap.variables.OvalVariableBindingResolver;
import com.example.scap.variables.ResolvedVariableBindings;
import com.example.scap.variables.VariableBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateCompileService {

    private final ContentPackageLoader contentPackageLoader;

    private final XccdfParser xccdfParser;
    private final OvalParser ovalParser;

    private final XccdfIndexBuilder xccdfIndexBuilder;
    private final OvalIndexBuilder ovalIndexBuilder;

    private final ProfileResolver profileResolver;
    private final RuleOvalReferenceResolver ruleOvalReferenceResolver;
    private final ReferencedOvalDefinitionResolver referencedOvalDefinitionResolver;
    private final OvalVariableBindingResolver ovalVariableBindingResolver;
    private final OvalEvaluationSliceResolver ovalEvaluationSliceResolver;
    private final OvalVariableClosureResolver ovalVariableClosureResolver;
    private final LocalVariablePlanCompiler localVariablePlanCompiler;

    private final OvalCheckCompilationService ovalCheckCompilationService;
    private final OvalDefinitionPlanCompiler ovalDefinitionPlanCompiler;

    //private final ExecutionTemplateStore executionTemplateStore;

    public CompileTemplateResponse compile(final CompileTemplateRequest request) throws Exception {
        //final ContentPackage contentPackage = contentPackageLoader.load(request.getBenchmarkId(), request.getProfileId());

        // Temporary until the content-package loader is wired in: fixture paths
        // relative to the repo root so local runs and CI behave the same.
        InputStream xccdf = new FileInputStream("src/test/resources/xccdf.xml");
        InputStream oval = new FileInputStream("src/test/resources/oval.xml");

        final ParsedXccdfBenchmark benchmark = xccdfParser.parse(xccdf);
        final ParsedOval ovalDefinitions = ovalParser.parse(oval);

        // Indexes are per content package, not singleton services.
        final XccdfIndex xccdfIndex = xccdfIndexBuilder.build(benchmark);
        final OvalIndex ovalIndex = ovalIndexBuilder.build(ovalDefinitions);

        final ResolvedProfile resolvedProfile = profileResolver.resolve(benchmark, request.getProfileId());
        final List<ResolvedRuleOvalRefs> ruleOvalRefs = ruleOvalReferenceResolver.resolve(resolvedProfile);
        final ResolvedOvalEvaluationSlice ovalSlice = referencedOvalDefinitionResolver.resolve(ovalIndex, ruleOvalRefs);

        final ResolvedVariableBindings variableBindings =
                ovalVariableBindingResolver.resolve(
                        benchmark,
                        resolvedProfile,
                        ruleOvalRefs,
                        xccdfIndex,
                        ovalIndex,
                        Collections.emptyMap()
                );

        final ResolvedOvalVariableClosure variableClosure =
                ovalVariableClosureResolver.resolve(ovalIndex, ovalSlice);

        final LocalVariableCompilationResult localVariables =
                localVariablePlanCompiler.compile(
                        ovalIndex,
                        variableClosure.getVariableIds()
                );

        final OvalCheckCompilationResult checkCompilationResult =
                ovalCheckCompilationService.compile(
                        ovalIndex,
                        ovalSlice,
                        variableBindings,
                        localVariables,
                        variableClosure.getObjectIds());

        final List<CompiledOvalDefinitionPlan> definitionPlans =
                ovalDefinitionPlanCompiler.compile(
                        ovalSlice.getDefinitions(),
                        checkCompilationResult
                );

        final ExecutionTemplate template =
                assembleTemplate(
                        request,
                        resolvedProfile,
                        ruleOvalRefs,
                        checkCompilationResult,
                        definitionPlans,
                        ovalIndex,
                        variableClosure,
                        variableBindings,
                        localVariables
                );

        // Later: persist to S3/LocalStack and return artifact location.
        writeToDisk(template, Path.of("./test.json"));
        return toResponse(template, "");
    }

    private ExecutionTemplate assembleTemplate(
            final CompileTemplateRequest request,
            final ResolvedProfile resolvedProfile,
            final List<ResolvedRuleOvalRefs> ruleOvalRefs,
            final OvalCheckCompilationResult checkCompilationResult,
            final List<CompiledOvalDefinitionPlan> definitionPlans,
            final OvalIndex ovalIndex,
            final ResolvedOvalVariableClosure variableClosure,
            final ResolvedVariableBindings variableBindings,
            final LocalVariableCompilationResult localVariables
    ) {
        final ExecutionTemplate template = new ExecutionTemplate();

        template.setTemplateId(UUID.randomUUID().toString());
        template.setBenchmarkId(resolvedProfile.getBenchmarkId());
        template.setProfileId(resolvedProfile.getProfileId());
        template.setGeneratedAt(Instant.now());

        for (final ResolvedXccdfRule rule : resolvedProfile.getSelectedRules()) {
            template.getRules().add(toCompiledTemplateRule(rule, ruleOvalRefs));
        }

        template.getDefinitionPlans().addAll(definitionPlans);
        template.getObjectsById().putAll(checkCompilationResult.getObjects());
        template.getStatesById().putAll(checkCompilationResult.getStates());
        template.getChecks().addAll(checkCompilationResult.getCompiledChecks());
        template.getUnsupportedCheckTypes().addAll(checkCompilationResult.getUnsupportedCheckTypes());
        template.getWarnings().addAll(checkCompilationResult.getWarnings());

        assembleVariables(template, ovalIndex, variableClosure, variableBindings, localVariables, checkCompilationResult);

        return template;
    }

    /**
     * Emits one entry per referenced variable: LITERAL when values were
     * resolved at compile time (external/constant), PLAN for local variables
     * the agent evaluates at runtime, UNRESOLVED otherwise. UNRESOLVED
     * variables make dependent tests evaluate to error on the agent.
     */
    private void assembleVariables(
            final ExecutionTemplate template,
            final OvalIndex ovalIndex,
            final ResolvedOvalVariableClosure variableClosure,
            final ResolvedVariableBindings variableBindings,
            final LocalVariableCompilationResult localVariables,
            final OvalCheckCompilationResult checkCompilationResult
    ) {
        for (final String variableId : variableClosure.getVariableIds()) {
            final ParsedOvalVariable parsedVariable = ovalIndex.getVariableById().get(variableId);
            final String datatype = parsedVariable != null && parsedVariable.getDatatype() != null
                    ? parsedVariable.getDatatype()
                    : "string";

            final CompiledVariable.CompiledVariableBuilder variable = CompiledVariable.builder()
                    .variableId(variableId)
                    .datatype(datatype);

            final String unresolvedReason = findUnresolvedReason(
                    variableId, variableClosure, localVariables, checkCompilationResult);
            final VariableBinding binding = variableBindings.getBindingsById().get(variableId);
            final CompiledLocalVariableExpression plan = localVariables.getLocalVariablesById().get(variableId);

            if (unresolvedReason != null) {
                variable.kind(CompiledVariableKind.UNRESOLVED);
                template.getWarnings().add("Variable " + variableId + " unresolved: " + unresolvedReason);
            } else if (plan != null) {
                variable.kind(CompiledVariableKind.PLAN).expression(plan.getExpression());
            } else if (binding != null) {
                variable.kind(CompiledVariableKind.LITERAL).values(binding.getValues());
            } else {
                variable.kind(CompiledVariableKind.UNRESOLVED);
                template.getWarnings().add("Variable " + variableId
                        + " unresolved: no XCCDF value or override bound to external variable");
            }

            template.getVariablesById().put(variableId, variable.build());
        }
    }

    private String findUnresolvedReason(
            final String variableId,
            final ResolvedOvalVariableClosure variableClosure,
            final LocalVariableCompilationResult localVariables,
            final OvalCheckCompilationResult checkCompilationResult
    ) {
        final String closureReason = variableClosure.getUnsupportedVariableReasons().get(variableId);
        if (closureReason != null) {
            return closureReason;
        }

        final String planReason = localVariables.getUnsupportedVariableReasons().get(variableId);
        if (planReason != null) {
            return planReason;
        }

        return variableClosure.getObjectIdsByVariableId()
                .getOrDefault(variableId, Set.of())
                .stream()
                .filter(checkCompilationResult.getFailedObjectIds()::contains)
                .findFirst()
                .map(objectId -> "depends on uncompilable object " + objectId)
                .orElse(null);
    }

    public static void writeToDisk(final ExecutionTemplate template, final Path outputPath) throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputPath.toFile(), template);
    }

    private CompiledTemplateRule toCompiledTemplateRule(
            final ResolvedXccdfRule rule,
            final List<ResolvedRuleOvalRefs> ruleOvalRefs) {
        final CompiledTemplateRule compiledRule = new CompiledTemplateRule();

        compiledRule.setRuleId(rule.getRuleId());
        compiledRule.setTitle(rule.getTitle());

        ruleOvalRefs.stream()
                .filter(refs -> rule.getRuleId().equals(refs.getRuleId()))
                .findFirst()
                .ifPresent(refs -> refs.getReferences().stream()
                        .map(ResolvedCheckReference::getName)
                        .filter(name -> name != null && !name.isBlank())
                        .forEach(compiledRule.getOvalDefinitionIds()::add));

        return compiledRule;
    }



    private CompileTemplateResponse toResponse(
            final ExecutionTemplate template,
            final String artifactLocation) {
        final CompileTemplateResponse response = new CompileTemplateResponse();

        response.setTemplateId(template.getTemplateId());
        response.setBenchmarkId(template.getBenchmarkId());
        response.setProfileId(template.getProfileId());

        response.setArtifactLocation(artifactLocation);
        response.setArtifactVersion(template.getSchemaVersion());

        response.setSelectedRuleCount(template.getRules().size());
        //response.setCollectionTaskCount(template.getCollectionTasks().size());
        response.setCompiledCheckCount(template.getChecks().size());
       // response.setDefinitionPlanCount(template.getDefinitionPlans().size());
        response.setUnsupportedTestCount(template.getUnsupportedCheckTypes().size());
        response.setUnsupportedTestsByFamily(template.getUnsupportedCheckTypes());

        /*
        response.setCompiledChecksByFamily(
                template.getCheckPlans().stream()
                        .collect(Collectors.groupingBy(
                                checkPlan -> checkPlan.getFamily(),
                                Collectors.counting()
                        ))
        );*/

        if (template.getWarnings() != null) {
            response.setWarnings(template.getWarnings());
        }

        return response;
    }
}
