package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.ParsedOvalObjectBase;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalTestDependencyResolverImplTest {
    @Test
    void resolve_shouldResolveSingleObjectAndSingleState() {
        OvalIndex index = new OvalIndex();

        ParsedOvalObjectBase object = object("oval:obj:1");
        ParsedOvalState state = state("oval:ste:1");

        index.getObjectById().put(object.getObjectId(), object);
        index.getStateById().put(state.getStateId(), state);

        ParsedOvalTest test = test("oval:tst:1", "oval:obj:1", "oval:ste:1");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        OvalTestDependencyResolver.Result result = resolver.resolve(index, List.of(test));

        assertEquals(List.of("oval:obj:1"),
                result.objects().stream().map(ParsedOvalObjectBase::getObjectId).toList());

        assertEquals(List.of("oval:ste:1"),
                result.states().stream().map(ParsedOvalState::getStateId).toList());
    }

    @Test
    void resolve_shouldResolveOneObjectAndMultipleStates() {
        OvalIndex index = new OvalIndex();

        ParsedOvalObjectBase object = object("oval:obj:1");
        ParsedOvalState state1 = state("oval:ste:1");
        ParsedOvalState state2 = state("oval:ste:2");

        index.getObjectById().put(object.getObjectId(), object);
        index.getStateById().put(state1.getStateId(), state1);
        index.getStateById().put(state2.getStateId(), state2);

        ParsedOvalTest test = test("oval:tst:1", "oval:obj:1", "oval:ste:1", "oval:ste:2");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        OvalTestDependencyResolver.Result result = resolver.resolve(index, List.of(test));

        assertEquals(List.of("oval:obj:1"),
                result.objects().stream().map(ParsedOvalObjectBase::getObjectId).toList());

        assertEquals(List.of("oval:ste:1", "oval:ste:2"),
                result.states().stream().map(ParsedOvalState::getStateId).toList());
    }

    @Test
    void resolve_shouldDeduplicateObjectsAndStatesPreservingFirstEncounterOrder() {
        OvalIndex index = new OvalIndex();

        ParsedOvalObjectBase object1 = object("oval:obj:1");
        ParsedOvalObjectBase object2 = object("oval:obj:2");

        ParsedOvalState state1 = state("oval:ste:1");
        ParsedOvalState state2 = state("oval:ste:2");

        index.getObjectById().put(object1.getObjectId(), object1);
        index.getObjectById().put(object2.getObjectId(), object2);

        index.getStateById().put(state1.getStateId(), state1);
        index.getStateById().put(state2.getStateId(), state2);

        ParsedOvalTest test1 = test("oval:tst:1", "oval:obj:1", "oval:ste:1");
        ParsedOvalTest test2 = test("oval:tst:2", "oval:obj:1", "oval:ste:1", "oval:ste:2");
        ParsedOvalTest test3 = test("oval:tst:3", "oval:obj:2", "oval:ste:2");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        OvalTestDependencyResolver.Result result = resolver.resolve(index, List.of(test1, test2, test3));

        assertEquals(List.of("oval:obj:2", "oval:obj:1"),
                result.objects().stream().map(ParsedOvalObjectBase::getObjectId).toList());

        assertEquals(List.of("oval:ste:1", "oval:ste:2"),
                result.states().stream().map(ParsedOvalState::getStateId).toList());
    }

    @Test
    void resolve_shouldIgnoreNullAndBlankObjectRefsAndStateRefs() {
        OvalIndex index = new OvalIndex();

        ParsedOvalObjectBase object = object("oval:obj:1");
        ParsedOvalState state = state("oval:ste:1");

        index.getObjectById().put(object.getObjectId(), object);
        index.getStateById().put(state.getStateId(), state);

        ParsedOvalTest test1 = test("oval:tst:1", null, "", "   ", "oval:ste:1");
        ParsedOvalTest test2 = test("oval:tst:2", "", "oval:ste:1");
        ParsedOvalTest test3 = test("oval:tst:3", "oval:obj:1");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        OvalTestDependencyResolver.Result result = resolver.resolve(index, List.of(test1, test2, test3));

        assertEquals(List.of("oval:obj:1"),
                result.objects().stream().map(ParsedOvalObjectBase::getObjectId).toList());

        assertEquals(List.of("oval:ste:1"),
                result.states().stream().map(ParsedOvalState::getStateId).toList());
    }

    @Test
    void resolve_shouldThrowWhenObjectIsMissing() {
        OvalIndex index = new OvalIndex();

        ParsedOvalTest test = test("oval:tst:1", "oval:obj:missing");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(index, List.of(test))
        );

        assertEquals("OVAL object not found: oval:obj:missing", ex.getMessage());
    }

    @Test
    void resolve_shouldThrowWhenStateIsMissing() {
        OvalIndex index = new OvalIndex();

        ParsedOvalObjectBase object = object("oval:obj:1");
        index.getObjectById().put(object.getObjectId(), object);

        ParsedOvalTest test = test("oval:tst:1", "oval:obj:1", "oval:ste:missing");

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(index, List.of(test))
        );

        assertEquals("OVAL state not found: oval:ste:missing", ex.getMessage());
    }

    @Test
    void resolve_shouldReturnEmptyResultWhenNoTestsProvided() {
        OvalIndex index = new OvalIndex();

        OvalTestDependencyResolver resolver = new OvalTestDependencyResolverImpl();

        OvalTestDependencyResolver.Result result = resolver.resolve(index, List.of());

        assertNotNull(result.objects());
        assertNotNull(result.states());
        assertTrue(result.objects().isEmpty());
        assertTrue(result.states().isEmpty());
    }

    private ParsedOvalTest test(String testId, String objectRef, String... stateRefs) {
        ParsedOvalTest test = new ParsedOvalTest();
        test.setId(testId);
        test.setObjectRef(objectRef);
        test.getStateRef().addAll(List.of(stateRefs));
        return test;
    }

    private ParsedOvalObjectBase object(String objectId) {
        ParsedOvalObjectBase object = new ParsedOvalObjectBase();
        object.setObjectId(objectId);
        return object;
    }

    private ParsedOvalState state(String stateId) {
        ParsedOvalState state = new ParsedOvalState();
        state.setStateId(stateId);
        return state;
    }
}