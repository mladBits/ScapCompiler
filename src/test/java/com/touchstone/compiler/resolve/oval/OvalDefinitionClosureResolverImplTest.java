package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteria;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriteriaNode;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalCriterion;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalDefinition;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalExtendedDefinition;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalDefinitionClosureResolverImplTest {

    private final OvalCriteriaTestRefCollector testRefCollector = new OvalCriteriaTestRefCollector();
    private final OvalDefinitionRefCollector definitionRefCollector = new OvalDefinitionRefCollector();

    @Test
    void resolve_shouldReturnSingleDefinitionAndItsTests() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria(
                criterion("oval:test:1"),
                criterion("oval:test:2")
        ));

        ParsedOvalTest test1 = test("oval:test:1");
        ParsedOvalTest test2 = test("oval:test:2");

        index.getDefinitionById().put(def1.getId(), def1);
        index.getTestById().put(test1.getId(), test1);
        index.getTestById().put(test2.getId(), test2);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        ResolvedOvalEvaluationSlice slice = resolver.resolve(index, List.of("oval:def:1"));

        assertEquals(List.of("oval:def:1"),
                slice.getDefinitions().stream().map(ParsedOvalDefinition::getId).toList());

        assertEquals(List.of("oval:test:1", "oval:test:2"),
                slice.getTests().stream().map(ParsedOvalTest::getId).toList());
    }

    @Test
    void resolve_shouldFollowExtendedDefinitionsTransitively() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria(
                criterion("oval:test:1"),
                extendDefinition("oval:def:2")
        ));
        ParsedOvalDefinition def2 = definition("oval:def:2", criteria(
                criterion("oval:test:2"),
                extendDefinition("oval:def:3")
        ));
        ParsedOvalDefinition def3 = definition("oval:def:3", criteria(
                criterion("oval:test:3")
        ));

        ParsedOvalTest test1 = test("oval:test:1");
        ParsedOvalTest test2 = test("oval:test:2");
        ParsedOvalTest test3 = test("oval:test:3");

        index.getDefinitionById().put(def1.getId(), def1);
        index.getDefinitionById().put(def2.getId(), def2);
        index.getDefinitionById().put(def3.getId(), def3);

        index.getTestById().put(test1.getId(), test1);
        index.getTestById().put(test2.getId(), test2);
        index.getTestById().put(test3.getId(), test3);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        ResolvedOvalEvaluationSlice slice = resolver.resolve(index, List.of("oval:def:1"));

        assertEquals(
                List.of("oval:def:1", "oval:def:2", "oval:def:3"),
                slice.getDefinitions().stream().map(ParsedOvalDefinition::getId).toList()
        );

        assertEquals(
                List.of("oval:test:1", "oval:test:2", "oval:test:3"),
                slice.getTests().stream().map(ParsedOvalTest::getId).toList()
        );
    }

    @Test
    void resolve_shouldDeduplicateDefinitionsAndTests() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria(
                criterion("oval:test:1"),
                extendDefinition("oval:def:2"),
                extendDefinition("oval:def:2")
        ));
        ParsedOvalDefinition def2 = definition("oval:def:2", criteria(
                criterion("oval:test:1"),
                criterion("oval:test:2")
        ));

        ParsedOvalTest test1 = test("oval:test:1");
        ParsedOvalTest test2 = test("oval:test:2");

        index.getDefinitionById().put(def1.getId(), def1);
        index.getDefinitionById().put(def2.getId(), def2);

        index.getTestById().put(test1.getId(), test1);
        index.getTestById().put(test2.getId(), test2);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        ResolvedOvalEvaluationSlice slice = resolver.resolve(index, List.of("oval:def:1", "oval:def:2"));

        assertEquals(
                List.of("oval:def:1", "oval:def:2"),
                slice.getDefinitions().stream().map(ParsedOvalDefinition::getId).toList()
        );

        assertEquals(
                List.of("oval:test:1", "oval:test:2"),
                slice.getTests().stream().map(ParsedOvalTest::getId).toList()
        );
    }

    @Test
    void resolve_shouldThrowWhenStartingDefinitionDoesNotExist() {
        OvalIndex index = new OvalIndex();

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(index, List.of("oval:def:missing"))
        );

        assertEquals("OVAL definition not found: oval:def:missing", ex.getMessage());
    }

    @Test
    void resolve_shouldThrowWhenReferencedChildDefinitionDoesNotExist() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria(
                extendDefinition("oval:def:missing")
        ));
        index.getDefinitionById().put(def1.getId(), def1);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(index, List.of("oval:def:1"))
        );

        assertEquals("OVAL definition not found: oval:def:missing", ex.getMessage());
    }

    @Test
    void resolve_shouldThrowWhenReferencedTestDoesNotExist() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria(
                criterion("oval:test:missing")
        ));
        index.getDefinitionById().put(def1.getId(), def1);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(index, List.of("oval:def:1"))
        );

        assertEquals("OVAL test not found: oval:test:missing", ex.getMessage());
    }

    @Test
    void resolve_shouldHandleDefinitionsWithNoTestsOrExtendedDefinitions() {
        OvalIndex index = new OvalIndex();

        ParsedOvalDefinition def1 = definition("oval:def:1", criteria());
        index.getDefinitionById().put(def1.getId(), def1);

        OvalDefinitionClosureResolverImpl resolver =
                new OvalDefinitionClosureResolverImpl(testRefCollector, definitionRefCollector);

        ResolvedOvalEvaluationSlice slice = resolver.resolve(index, List.of("oval:def:1"));

        assertEquals(1, slice.getDefinitions().size());
        assertEquals("oval:def:1", slice.getDefinitions().getFirst().getId());
        assertTrue(slice.getTests().isEmpty());
    }

    private ParsedOvalDefinition definition(String definitionId, ParsedOvalCriteria criteria) {
        ParsedOvalDefinition definition = new ParsedOvalDefinition();
        definition.setId(definitionId);
        definition.setCriteria(criteria);
        return definition;
    }

    private ParsedOvalTest test(String testId) {
        ParsedOvalTest test = new ParsedOvalTest();
        test.setId(testId);
        return test;
    }

    private ParsedOvalCriterion criterion(String testRef) {
        ParsedOvalCriterion criterion = new ParsedOvalCriterion();
        criterion.setTestRef(testRef);
        return criterion;
    }

    private ParsedOvalExtendedDefinition extendDefinition(String definitionRef) {
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