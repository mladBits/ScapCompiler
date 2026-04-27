package com.example.scap.oval.definition;

import com.example.scap.model.parsed.oval.*;
import com.example.scap.oval.CompiledOvalCheck;
import com.example.scap.oval.OvalCheckCompilationResult;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class OvalDefinitionPlanCompilerImpl implements OvalDefinitionPlanCompiler {

    @Override
    public List<CompiledOvalDefinitionPlan> compile(
            final Collection<ParsedOvalDefinition> definitions,
            final OvalCheckCompilationResult checkCompilationResult) {
        final Set<String> compiledTestIds = new HashSet<>(
                checkCompilationResult.getCompiledChecks().stream()
                        .map(CompiledOvalCheck::getTestId)
                        .toList()
        );

        return definitions.stream()
                .map(definition -> compileDefinition(definition, compiledTestIds))
                .toList();
    }

    private CompiledOvalDefinitionPlan compileDefinition(
            final ParsedOvalDefinition definition,
            final Set<String> compiledTestIds) {
        final CompiledOvalDefinitionPlan plan = new CompiledOvalDefinitionPlan();

        plan.setId(definition.getId());
        plan.setDefClass(definition.getDefClass());
        plan.setTitle("");
        plan.setCriteria(compileCriteriaNode(definition.getCriteria(), compiledTestIds));

        return plan;
    }

    private CompiledOvalCriteriaNode compileCriteriaNode(
            final ParsedOvalCriteriaNode node,
            final Set<String> compiledTestIds) {
        if (node instanceof ParsedOvalCriteria criteria) {
            return compileCriteria(criteria, compiledTestIds);
        }

        if (node instanceof ParsedOvalCriterion criterion) {
            return compileCriterion(criterion, compiledTestIds);
        }

        if (node instanceof ParsedOvalExtendedDefinition extendedDefinition) {
            return compileExtendedDefinition(extendedDefinition);
        }

        throw new IllegalArgumentException("Unsupported OVAL criteria node: " + node.getClass().getName());
    }

    private CompiledOvalCriteriaGroup compileCriteria(
            final ParsedOvalCriteria criteria,
            final Set<String> compiledTestIds) {
        final CompiledOvalCriteriaGroup group = new CompiledOvalCriteriaGroup();

        group.setOperator(criteria.getOperator() == null ? "AND" : criteria.getOperator());
        group.setNegate(criteria.getIsNegated());

        for (final ParsedOvalCriteriaNode child : criteria.getChildren()) {
            group.getChildren().add(compileCriteriaNode(child, compiledTestIds));
        }

        return group;
    }

    private CompiledOvalCriterionRef compileCriterion(
            final ParsedOvalCriterion criterion,
            final Set<String> compiledTestIds) {
        final CompiledOvalCriterionRef ref = new CompiledOvalCriterionRef();

        ref.setTestId(criterion.getTestRef());
        ref.setNegate(criterion.getIsNegated());
        ref.setSupported(compiledTestIds.contains(criterion.getTestRef()));

        return ref;
    }

    private CompiledOvalExtendDefinitionRef compileExtendedDefinition(
            final ParsedOvalExtendedDefinition extendedDefinition) {
        final CompiledOvalExtendDefinitionRef ref = new CompiledOvalExtendDefinitionRef();

        ref.setDefinitionId(extendedDefinition.getDefinitionRef());
        ref.setNegate(extendedDefinition.getIsNegated());

        return ref;
    }
}
