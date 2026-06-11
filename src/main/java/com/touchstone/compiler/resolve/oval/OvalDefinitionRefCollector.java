package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteria;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteriaNode;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriterion;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalExtendedDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class OvalDefinitionRefCollector {
    public Set<String> collect(final ParsedOvalCriteriaNode node) {
        final Set<String> collected = new LinkedHashSet<>();
        collectInto(node, collected);
        return collected;
    }

    public Set<String> collect(final Collection<? extends ParsedOvalCriteriaNode> nodes) {
        final Set<String> collected = new LinkedHashSet<>();

        for (final ParsedOvalCriteriaNode node : nodes) {
            collectInto(node, collected);
        }

        return collected;
    }

    private void collectInto(final ParsedOvalCriteriaNode node, final Set<String> collected) {
        switch (node) {
            case ParsedOvalExtendedDefinition extendedDefinition -> {
                final String definitionRef = extendedDefinition.getDefinitionRef();
                if (definitionRef != null && !definitionRef.isBlank()) {
                    collected.add(definitionRef);
                }
            }
            case ParsedOvalCriteria criteria -> {
                for (final ParsedOvalCriteriaNode child : criteria.getChildren()) {
                    collectInto(child, collected);
                }
            }
            case ParsedOvalCriterion criterion -> {
                // not relevant for definition ref collection
            }
            case null, default -> {
            }
        }

    }
}
