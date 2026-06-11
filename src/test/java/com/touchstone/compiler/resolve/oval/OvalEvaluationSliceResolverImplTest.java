package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.*;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OvalEvaluationSliceResolverImplTest {
    @Test
    void resolve_shouldCombineDefinitionSliceWithTestDependencies() {
        OvalIndex ovalIndex = new OvalIndex();

        ParsedOvalDefinition definition = definition("oval:def:1");
        ParsedOvalTest test = test("oval:tst:1");
        ParsedOvalObjectBase object = object("oval:obj:1");
        ParsedOvalState state = state("oval:ste:1");

        StubDefinitionClosureResolver definitionClosureResolver =
                new StubDefinitionClosureResolver(
                        new ResolvedOvalEvaluationSlice(
                                List.of(definition),
                                List.of(test),
                                List.of(),
                                List.of()
                        )
                );

        StubTestDependencyResolver testDependencyResolver =
                new StubTestDependencyResolver(
                        new OvalTestDependencyResolver.Result(
                                List.of(object),
                                List.of(state)
                        )
                );

        OvalEvaluationSliceResolver resolver =
                new OvalEvaluationSliceResolverImpl(definitionClosureResolver, testDependencyResolver);

        ResolvedOvalEvaluationSlice result = resolver.resolve(ovalIndex, List.of("oval:def:1"));

        assertEquals(List.of("oval:def:1"),
                result.getDefinitions().stream().map(ParsedOvalDefinition::getId).toList());

        assertEquals(List.of("oval:tst:1"),
                result.getTests().stream().map(ParsedOvalTest::getId).toList());

        assertEquals(List.of("oval:obj:1"),
                result.getObjects().stream().map(ParsedOvalObjectBase::getObjectId).toList());

        assertEquals(List.of("oval:ste:1"),
                result.getStates().stream().map(ParsedOvalState::getStateId).toList());

        assertSame(ovalIndex, definitionClosureResolver.capturedOvalIndex);
        assertSame(ovalIndex, testDependencyResolver.capturedOvalIndex);

        assertEquals(List.of("oval:def:1"), definitionClosureResolver.capturedStartingDefinitionIds);
        assertEquals(List.of("oval:tst:1"),
                testDependencyResolver.capturedTests.stream().map(ParsedOvalTest::getId).toList());
    }

    @Test
    void resolve_shouldHandleEmptyDefinitionSlice() {
        OvalIndex ovalIndex = new OvalIndex();

        StubDefinitionClosureResolver definitionClosureResolver =
                new StubDefinitionClosureResolver(
                        new ResolvedOvalEvaluationSlice(
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of()
                        )
                );

        StubTestDependencyResolver testDependencyResolver =
                new StubTestDependencyResolver(
                        new OvalTestDependencyResolver.Result(
                                List.of(),
                                List.of()
                        )
                );

        OvalEvaluationSliceResolver resolver =
                new OvalEvaluationSliceResolverImpl(definitionClosureResolver, testDependencyResolver);

        ResolvedOvalEvaluationSlice result = resolver.resolve(ovalIndex, List.of());

        assertTrue(result.getDefinitions().isEmpty());
        assertTrue(result.getTests().isEmpty());
        assertTrue(result.getObjects().isEmpty());
        assertTrue(result.getStates().isEmpty());

        assertSame(ovalIndex, definitionClosureResolver.capturedOvalIndex);
        assertSame(ovalIndex, testDependencyResolver.capturedOvalIndex);

        assertNotNull(definitionClosureResolver.capturedStartingDefinitionIds);
        assertTrue(definitionClosureResolver.capturedStartingDefinitionIds.isEmpty());

        assertNotNull(testDependencyResolver.capturedTests);
        assertTrue(testDependencyResolver.capturedTests.isEmpty());
    }

    @Test
    void resolve_shouldPreserveDefinitionsAndTestsFromDefinitionClosureResolver() {
        OvalIndex ovalIndex = new OvalIndex();

        ParsedOvalDefinition definition1 = definition("oval:def:1");
        ParsedOvalDefinition definition2 = definition("oval:def:2");

        ParsedOvalTest test1 = test("oval:tst:1");
        ParsedOvalTest test2 = test("oval:tst:2");

        StubDefinitionClosureResolver definitionClosureResolver =
                new StubDefinitionClosureResolver(
                        new ResolvedOvalEvaluationSlice(
                                List.of(definition1, definition2),
                                List.of(test1, test2),
                                List.of(),
                                List.of()
                        )
                );

        StubTestDependencyResolver testDependencyResolver =
                new StubTestDependencyResolver(
                        new OvalTestDependencyResolver.Result(
                                List.of(),
                                List.of()
                        )
                );

        OvalEvaluationSliceResolver resolver =
                new OvalEvaluationSliceResolverImpl(definitionClosureResolver, testDependencyResolver);

        ResolvedOvalEvaluationSlice result = resolver.resolve(ovalIndex, List.of("oval:def:1"));

        assertEquals(List.of("oval:def:1", "oval:def:2"),
                result.getDefinitions().stream().map(ParsedOvalDefinition::getId).toList());

        assertEquals(List.of("oval:tst:1", "oval:tst:2"),
                result.getTests().stream().map(ParsedOvalTest::getId).toList());

        assertSame(ovalIndex, definitionClosureResolver.capturedOvalIndex);
        assertSame(ovalIndex, testDependencyResolver.capturedOvalIndex);
    }

    private ParsedOvalDefinition definition(String definitionId) {
        ParsedOvalDefinition definition = new ParsedOvalDefinition();
        definition.setId(definitionId);
        return definition;
    }

    private ParsedOvalTest test(String testId) {
        ParsedOvalTest test = new ParsedOvalTest();
        test.setId(testId);
        return test;
    }

    private ParsedOvalObjectBase object(String objectId) {
        ParsedOvalObjectBase object = new ParsedOvalObject();
        object.setObjectId(objectId);
        return object;

    }

    private ParsedOvalState state(String stateId) {
        ParsedOvalState state = new ParsedOvalState();
        state.setStateId(stateId);
        return state;
    }

    private static class StubDefinitionClosureResolver implements OvalDefinitionClosureResolver {
        private final ResolvedOvalEvaluationSlice returnValue;
        private OvalIndex capturedOvalIndex;
        private List<String> capturedStartingDefinitionIds;

        private StubDefinitionClosureResolver(final ResolvedOvalEvaluationSlice returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public ResolvedOvalEvaluationSlice resolve(
                final OvalIndex ovalIndex,
                final Collection<String> startingDefinitionIds
        ) {
            this.capturedOvalIndex = ovalIndex;
            this.capturedStartingDefinitionIds = new ArrayList<>(startingDefinitionIds);
            return returnValue;
        }
    }

    private static class StubTestDependencyResolver implements OvalTestDependencyResolver {
        private final Result returnValue;
        private OvalIndex capturedOvalIndex;
        private List<ParsedOvalTest> capturedTests;

        private StubTestDependencyResolver(final Result returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public Result resolve(
                final OvalIndex ovalIndex,
                final Collection<ParsedOvalTest> tests
        ) {
            this.capturedOvalIndex = ovalIndex;
            this.capturedTests = new ArrayList<>(tests);
            return returnValue;
        }
    }
}