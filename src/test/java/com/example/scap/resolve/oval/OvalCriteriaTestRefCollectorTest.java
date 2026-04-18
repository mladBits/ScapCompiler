package com.example.scap.resolve.oval;

import com.example.scap.model.parsed.oval.ParsedOvalCriteria;
import com.example.scap.model.parsed.oval.ParsedOvalCriteriaNode;
import com.example.scap.model.parsed.oval.ParsedOvalCriterion;
import com.example.scap.model.parsed.oval.ParsedOvalExtendedDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalCriteriaTestRefCollectorTest {
    private final OvalCriteriaTestRefCollector collector = new OvalCriteriaTestRefCollector();

    @Test
    void collect_shouldReturnSingleTestRef_forCriterionNode() {
        ParsedOvalCriterion criterion = criterion("oval:test:1");

        Set<String> result = collector.collect(criterion);

        assertEquals(Set.of("oval:test:1"), result);
    }

    @Test
    void collect_shouldReturnEmptyList_forExtendedDefinitionNode() {
        ParsedOvalExtendedDefinition extendedDefinition = extendedDefinition("oval:def:1");

        Set<String> result = collector.collect(extendedDefinition);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_shouldFlattenNestedCriteriaTree_preservingEncounterOrder() {
        ParsedOvalCriterion c1 = criterion("oval:test:1");
        ParsedOvalCriterion c2 = criterion("oval:test:2");
        ParsedOvalCriterion c3 = criterion("oval:test:3");

        ParsedOvalCriteria nested = criteria(c2, c3);
        ParsedOvalCriteria root = criteria(c1, nested);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:test:1", "oval:test:2", "oval:test:3"), result);
    }

    @Test
    void collect_shouldDeduplicateDuplicateTestRefs() {
        ParsedOvalCriterion c1 = criterion("oval:test:1");
        ParsedOvalCriterion c2 = criterion("oval:test:1");
        ParsedOvalCriterion c3 = criterion("oval:test:2");

        ParsedOvalCriteria root = criteria(c1, c2, c3);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:test:1", "oval:test:2"), result);
    }

    @Test
    void collect_shouldIgnoreNullAndBlankTestRefs() {
        ParsedOvalCriterion nullRef = criterion(null);
        ParsedOvalCriterion blankRef = criterion("   ");
        ParsedOvalCriterion validRef = criterion("oval:test:1");

        ParsedOvalCriteria root = criteria(nullRef, blankRef, validRef);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:test:1"), result);
    }

    @Test
    void collect_shouldHandleCollectionOverload() {
        ParsedOvalCriterion c1 = criterion("oval:test:1");
        ParsedOvalExtendedDefinition ext = extendedDefinition("oval:def:1");
        ParsedOvalCriteria nested = criteria(criterion("oval:test:2"));

        Set<String> result = collector.collect(List.of(c1, ext, nested));

        assertEquals(Set.of("oval:test:1", "oval:test:2"), result);
    }

    @Test
    void collect_shouldHandleEmptyCollection() {
        Set<String> result = collector.collect(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_shouldHandleNullNode() {
        Set<String> result = collector.collect((ParsedOvalCriteriaNode) null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private ParsedOvalCriterion criterion(String testRef) {
        ParsedOvalCriterion criterion = new ParsedOvalCriterion();
        criterion.setTestRef(testRef);
        return criterion;
    }

    private ParsedOvalExtendedDefinition extendedDefinition(String definitionRef) {
        ParsedOvalExtendedDefinition extendedDefinition = new ParsedOvalExtendedDefinition();
        extendedDefinition.setDefinitionRef(definitionRef);
        return extendedDefinition;
    }

    private ParsedOvalCriteria criteria(ParsedOvalCriteriaNode... children) {
        ParsedOvalCriteria criteria = new ParsedOvalCriteria();
        criteria.getChildren().addAll(List.of(children));
        return criteria;
    }
}