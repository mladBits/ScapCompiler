package com.example.scap.service;

import com.example.scap.api.dto.CompileTemplateRequest;
import com.example.scap.api.dto.CompileTemplateResponse;
import com.example.scap.content.ContentPackageLoader;
import com.example.scap.index.OvalIndex;
import com.example.scap.index.OvalIndexBuilder;
import com.example.scap.index.XccdfBenchmarkIndex;
import com.example.scap.index.XccdfBenchmarkIndexBuilder;
import com.example.scap.model.compiled.CompiledTemplateRule;
import com.example.scap.model.compiled.ExecutionTemplate;
import com.example.scap.model.compiled.variables.LocalVariablePlanCompiler;
import com.example.scap.model.parsed.oval.ParsedOval;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
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
import com.example.scap.resolve.oval.ReferencedOvalDefinitionResolver;
import com.example.scap.resolve.xccdf.ProfileResolver;
import com.example.scap.resolve.xccdf.RuleOvalReferenceResolver;
import com.example.scap.variables.OvalVariableBindingResolver;
import com.example.scap.variables.ResolvedVariableBindings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateCompileService {

    private final ContentPackageLoader contentPackageLoader;

    private final XccdfParser xccdfParser;
    private final OvalParser ovalParser;

    private final XccdfBenchmarkIndexBuilder xccdfIndexBuilder;
    private final OvalIndexBuilder ovalIndexBuilder;

    private final ProfileResolver profileResolver;
    private final RuleOvalReferenceResolver ruleOvalReferenceResolver;
    private final ReferencedOvalDefinitionResolver referencedOvalDefinitionResolver;
    private final OvalVariableBindingResolver ovalVariableBindingResolver;
    private final OvalEvaluationSliceResolver ovalEvaluationSliceResolver;
    private final LocalVariablePlanCompiler localVariablePlanCompiler;

    private final OvalCheckCompilationService ovalCheckCompilationService;
    private final OvalDefinitionPlanCompiler ovalDefinitionPlanCompiler;

    //private final ExecutionTemplateStore executionTemplateStore;

    public CompileTemplateResponse compile(final CompileTemplateRequest request) throws FileNotFoundException {
        //final ContentPackage contentPackage = contentPackageLoader.load(request.getBenchmarkId(), request.getProfileId());

        InputStream xccdf = new FileInputStream("C:\\Users\\_mlad\\Documents\\GitHub\\ScapCompiler\\src\\test\\resources\\xccdf.xml");
        InputStream oval = new FileInputStream("C:\\Users\\_mlad\\Documents\\GitHub\\ScapCompiler\\src\\test\\resources\\oval.xml");

        final ParsedXccdfBenchmark benchmark = xccdfParser.parse(xccdf);
        final ParsedOval ovalDefinitions = ovalParser.parse(oval);

        // Indexes are per content package, not singleton services.
        final XccdfBenchmarkIndex xccdfIndex = xccdfIndexBuilder.build(benchmark);
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

        final OvalCheckCompilationResult checkCompilationResult =
                ovalCheckCompilationService.compile(
                        ovalIndex,
                        ovalSlice,
                        variableBindings,
                        null);

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
                        definitionPlans
                );

        // Later: persist to S3/LocalStack and return artifact location.
        return toResponse(template, "");
    }

    private ExecutionTemplate assembleTemplate(
            final CompileTemplateRequest request,
            final ResolvedProfile resolvedProfile,
            final List<ResolvedRuleOvalRefs> ruleOvalRefs,
            final OvalCheckCompilationResult checkCompilationResult,
            final List<CompiledOvalDefinitionPlan> definitionPlans
    ) {
        final ExecutionTemplate template = new ExecutionTemplate();

        template.setTemplateId(UUID.randomUUID().toString());
        template.setBenchmarkId(resolvedProfile.getBenchmarkId());
        template.setProfileId(resolvedProfile.getProfileId());
        template.setGeneratedAt(Instant.now());

        for (final ResolvedXccdfRule rule : resolvedProfile.getSelectedRules()) {
            template.getRules().add(toCompiledTemplateRule(rule, ruleOvalRefs));
        }

        template.getChecks().addAll(checkCompilationResult.getCompiledChecks());
        template.getUnsupportedCheckTypes().addAll(checkCompilationResult.getUnsupportedCheckTypes());

        return template;
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
