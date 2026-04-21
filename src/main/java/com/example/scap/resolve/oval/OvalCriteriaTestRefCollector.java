package com.example.scap.resolve.oval;

import com.example.scap.model.parsed.oval.ParsedOvalCriteria;
import com.example.scap.model.parsed.oval.ParsedOvalCriteriaNode;
import com.example.scap.model.parsed.oval.ParsedOvalCriterion;
import com.example.scap.model.parsed.oval.ParsedOvalExtendedDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class OvalCriteriaTestRefCollector {
    public Set<String> collect(final ParsedOvalCriteriaNode root) {
        final Set<String> testRefs = new LinkedHashSet<>();
        collectInto(root, testRefs);
        return testRefs;
    }

    public Set<String> collect(final Collection<ParsedOvalCriteriaNode> roots) {
        final Set<String> testRefs = new LinkedHashSet<>();
        roots.forEach(rootNode -> collectInto(rootNode, testRefs));
        return testRefs;
    }

    private void collectInto(final ParsedOvalCriteriaNode node, final Set<String> testRefs) {
        switch (node) {
            case ParsedOvalCriterion criterion -> {
                final String testRef = criterion.getTestRef();
                if (testRef != null && !testRef.isBlank())
                    testRefs.add(criterion.getTestRef());
            }
            case ParsedOvalCriteria criteria ->
                    criteria.getChildren()
                            .forEach(childNode -> collectInto(childNode, testRefs));
            case null, default -> {
            }
        }

    }
}
