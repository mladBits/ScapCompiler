package com.example.scap.service;

import com.example.scap.api.dto.CompileTemplateRequest;
import com.example.scap.api.dto.CompileTemplateResponse;
import com.example.scap.content.ContentPackage;
import com.example.scap.content.ContentPackageLoader;
import com.example.scap.index.OvalIndex;
import com.example.scap.index.OvalIndexBuilder;
import com.example.scap.index.XccdfBenchmarkIndexBuilder;
import com.example.scap.model.compiled.CompiledTemplate;
import com.example.scap.model.compiled.CompiledTemplateRule;
import com.example.scap.model.parsed.oval.ParsedOval;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import com.example.scap.oval.CompiledOvalCheck;
import com.example.scap.oval.OvalCheckCompilationResult;
import com.example.scap.oval.OvalCheckCompilationService;
import com.example.scap.oval.definition.CompiledOvalDefinitionPlan;
import com.example.scap.oval.definition.OvalDefinitionPlanCompiler;
import com.example.scap.parser.OvalParser;
import com.example.scap.parser.XccdfParser;
import com.example.scap.resolve.oval.ReferencedOvalDefinitionResolver;
import com.example.scap.resolve.xccdf.ProfileResolver;
import com.example.scap.resolve.xccdf.RuleOvalReferenceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final OvalCheckCompilationService ovalCheckCompilationService;
    private final OvalDefinitionPlanCompiler ovalDefinitionPlanCompiler;

    public CompileTemplateResponse compile(final CompileTemplateRequest request) {
        final ContentPackage contentPackage = contentPackageLoader.load(
                request.getBenchmarkId(),
                request.getProfileId());

        final ParsedXccdfBenchmark benchmark = xccdfParser.parse(contentPackage.xccdfStream());
        final ParsedOval ovalDefinitions = ovalParser.parse(contentPackage.ovalStream());

        // Indexes are per content package, not singleton services.
        xccdfIndexBuilder.build(benchmark);
        final OvalIndex ovalIndex = ovalIndexBuilder.build(ovalDefinitions);

        final ResolvedProfile resolvedProfile = profileResolver.resolve(benchmark, request.getProfileId());
        final List<ResolvedRuleOvalRefs> ruleOvalRefs = ruleOvalReferenceResolver.resolve(resolvedProfile);
        final ResolvedOvalEvaluationSlice ovalSlice = referencedOvalDefinitionResolver.resolve(ovalIndex, ruleOvalRefs);
        final OvalCheckCompilationResult checkCompilationResult = ovalCheckCompilationService.compile(ovalIndex, ovalSlice);

        final List<CompiledOvalDefinitionPlan> definitionPlans =
                ovalDefinitionPlanCompiler.compile(
                        ovalSlice.getDefinitions(),
                        checkCompilationResult
                );

        final CompiledTemplate template =
                assembleTemplate(
                        request,
                        resolvedProfile,
                        ruleOvalRefs,
                        checkCompilationResult,
                        definitionPlans
                );

        // Later: persist to S3/LocalStack and return artifact location.
        return toResponse(template);
    }

    private CompiledTemplate assembleTemplate(
            final CompileTemplateRequest request,
            final ResolvedProfile resolvedProfile,
            final List<ResolvedRuleOvalRefs> ruleOvalRefs,
            final OvalCheckCompilationResult checkCompilationResult,
            final List<CompiledOvalDefinitionPlan> definitionPlans
    ) {
        final CompiledTemplate template = new CompiledTemplate();

        template.setTemplateId(UUID.randomUUID().toString());
        template.setBenchmarkId(resolvedProfile.getBenchmarkId());
        template.setProfileId(resolvedProfile.getProfileId());
        template.setGeneratedAt(Instant.now());

        for (final ResolvedXccdfRule rule : resolvedProfile.getSelectedRules()) {
            template.getRules().add(toCompiledTemplateRule(rule, ruleOvalRefs));
        }

        template.getCompiledChecks().addAll(checkCompilationResult.getCompiledChecks());
        template.getUnsupportedTestIds().addAll(checkCompilationResult.getUnsupportedTestIds());
        template.getDefinitionPlans().addAll(definitionPlans);

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

    private CompileTemplateResponse toResponse(final CompiledTemplate template) {
        final CompileTemplateResponse response = new CompileTemplateResponse();

        response.setTemplateId(template.getTemplateId());
        response.setBenchmarkId(template.getBenchmarkId());
        response.setProfileId(template.getProfileId());
        response.setSelectedRuleCount(template.getRules().size());
        response.setCompiledCheckCount(template.getCompiledChecks().size());
        response.setDefinitionPlanCount(template.getDefinitionPlans().size());
        response.setUnsupportedTestCount(template.getUnsupportedTestIds().size());

        response.setCompiledChecksByFamily(
                template.getCompiledChecks().stream()
                        .collect(Collectors.groupingBy(CompiledOvalCheck::family, Collectors.counting()
                        ))
        );

        return response;
    }
}
