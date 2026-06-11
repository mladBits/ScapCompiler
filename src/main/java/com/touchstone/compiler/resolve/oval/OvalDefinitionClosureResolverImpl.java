package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalDefinition;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class OvalDefinitionClosureResolverImpl implements OvalDefinitionClosureResolver {
    private final OvalCriteriaTestRefCollector testRefCollector;
    private final OvalDefinitionRefCollector definitionRefCollector;

    @Override
    public ResolvedOvalEvaluationSlice resolve(final OvalIndex ovalIndex, final Collection<String> startingDefinitionIds) {
        final Set<String> visitedDefinitionIds = new LinkedHashSet<>();
        final Set<String> referencedTestIds = new LinkedHashSet<>();
        final Queue<String> pendingDefinitionIds = new ArrayDeque<>(startingDefinitionIds);
        while (!pendingDefinitionIds.isEmpty()) {
            final String defId = pendingDefinitionIds.poll();
            if (!visitedDefinitionIds.add(defId)) {
                continue;
            }

            final ParsedOvalDefinition definition = ovalIndex.getDefinitionById().get(defId);
            if (definition == null) {
                throw new IllegalArgumentException("OVAL definition not found: " + defId);
            }

            final Set<String> childDefIds = definitionRefCollector.collect(definition.getCriteria());
            childDefIds.forEach(pendingDefinitionIds::offer);

            final Set<String> testIds = testRefCollector.collect(definition.getCriteria());
            referencedTestIds.addAll(testIds);
        }

        final List<ParsedOvalDefinition> definitions = visitedDefinitionIds.stream()
                .map(definitionId -> {
                    final ParsedOvalDefinition definition = ovalIndex.getDefinitionById().get(definitionId);
                    if (definition == null) {
                        throw new IllegalArgumentException("OVAL definition not found during materialization: " + definitionId);
                    }
                    return definition;
                })
                .toList();

        final List<ParsedOvalTest> tests = referencedTestIds.stream()
                .map(testId -> {
                    final ParsedOvalTest test = ovalIndex.getTestById().get(testId);
                    if (test == null) {
                        throw new IllegalArgumentException("OVAL test not found: " + testId);
                    }
                    return test;
                })
                .toList();

        return new ResolvedOvalEvaluationSlice(definitions, tests);
    }
}
