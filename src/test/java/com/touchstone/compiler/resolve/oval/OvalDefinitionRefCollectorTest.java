package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteria;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteriaNode;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriterion;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalExtendedDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalDefinitionRefCollectorTest {
    private final OvalDefinitionRefCollector collector = new OvalDefinitionRefCollector();

    @Test
    void collect_shouldReturnSingleDefinitionRef_forExtendedDefinitionNode() {
        ParsedOvalExtendedDefinition extendedDefinition = extendedDefinition("oval:def:1");

        Set<String> result = collector.collect(extendedDefinition);

        assertEquals(Set.of("oval:def:1"), result);
    }

    @Test
    void collect_shouldReturnEmptyList_forCriterionNode() {
        ParsedOvalCriterion criterion = criterion("oval:test:1");

        Set<String> result = collector.collect(criterion);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_shouldFlattenNestedCriteriaTree_preservingEncounterOrder() {
        ParsedOvalExtendedDefinition d1 = extendedDefinition("oval:def:1");
        ParsedOvalExtendedDefinition d2 = extendedDefinition("oval:def:2");
        ParsedOvalExtendedDefinition d3 = extendedDefinition("oval:def:3");

        ParsedOvalCriteria nested = criteria(d2, d3);
        ParsedOvalCriteria root = criteria(d1, nested);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:def:1", "oval:def:2", "oval:def:3"), result);
    }

    @Test
    void collect_shouldDeduplicateDuplicateDefinitionRefs() {
        ParsedOvalExtendedDefinition d1 = extendedDefinition("oval:def:1");
        ParsedOvalExtendedDefinition d2 = extendedDefinition("oval:def:1");
        ParsedOvalExtendedDefinition d3 = extendedDefinition("oval:def:2");

        ParsedOvalCriteria root = criteria(d1, d2, d3);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:def:1", "oval:def:2"), result);
    }

    @Test
    void collect_shouldIgnoreNullAndBlankDefinitionRefs() {
        ParsedOvalExtendedDefinition nullRef = extendedDefinition(null);
        ParsedOvalExtendedDefinition blankRef = extendedDefinition("   ");
        ParsedOvalExtendedDefinition validRef = extendedDefinition("oval:def:1");

        ParsedOvalCriteria root = criteria(nullRef, blankRef, validRef);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:def:1"), result);
    }

    @Test
    void collect_shouldIgnoreCriterionNodesInsideCriteria() {
        ParsedOvalCriterion criterion = criterion("oval:test:1");
        ParsedOvalExtendedDefinition ext = extendedDefinition("oval:def:1");

        ParsedOvalCriteria root = criteria(criterion, ext);

        Set<String> result = collector.collect(root);

        assertEquals(Set.of("oval:def:1"), result);
    }

    @Test
    void collect_shouldHandleCollectionOverload() {
        ParsedOvalExtendedDefinition d1 = extendedDefinition("oval:def:1");
        ParsedOvalCriterion criterion = criterion("oval:test:1");
        ParsedOvalCriteria nested = criteria(extendedDefinition("oval:def:2"));

        Set<String> result = collector.collect(List.of(d1, criterion, nested));

        assertEquals(Set.of("oval:def:1", "oval:def:2"), result);
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