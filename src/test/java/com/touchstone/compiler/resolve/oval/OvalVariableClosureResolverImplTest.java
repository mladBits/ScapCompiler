package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalEntity;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalFilter;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedConcatComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedObjectComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalLocalVariable;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedUnsupportedComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedVariableComponent;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalVariableClosure;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalVariableClosureResolverImplTest {

    private final OvalVariableClosureResolverImpl resolver = new OvalVariableClosureResolverImpl();

    @Test
    void resolve_shouldCollectVariableReferencedObjects() {
        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:1",
                localVariable("oval:t:var:1", objectComponent("oval:t:obj:99")));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(objectWithVarRef("oval:t:obj:1", "name", "oval:t:var:1")),
                List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getVariableIds().contains("oval:t:var:1"));
        assertTrue(closure.getObjectIds().contains("oval:t:obj:99"));
        assertEquals(Set.of("oval:t:obj:99"),
                closure.getObjectIdsByVariableId().get("oval:t:var:1"));
        assertTrue(closure.getUnsupportedVariableReasons().isEmpty());
    }

    @Test
    void resolve_shouldFollowVariableToVariableChains() {
        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:1",
                localVariable("oval:t:var:1", variableComponent("oval:t:var:2")));
        index.getVariableById().put("oval:t:var:2",
                localVariable("oval:t:var:2", objectComponent("oval:t:obj:99")));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(objectWithVarRef("oval:t:obj:1", "name", "oval:t:var:1")),
                List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getVariableIds().containsAll(Set.of("oval:t:var:1", "oval:t:var:2")));
        assertTrue(closure.getObjectIds().contains("oval:t:obj:99"));
    }

    @Test
    void resolve_shouldScanEntitiesOfVariableReferencedObjects() {
        // var:1 -> obj:99, and obj:99 itself references var:2
        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:1",
                localVariable("oval:t:var:1", objectComponent("oval:t:obj:99")));
        index.getVariableById().put("oval:t:var:2",
                localVariable("oval:t:var:2", variableComponent("oval:t:var:3")));
        index.getObjectById().put("oval:t:obj:99",
                objectWithVarRef("oval:t:obj:99", "key", "oval:t:var:2"));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(objectWithVarRef("oval:t:obj:1", "name", "oval:t:var:1")),
                List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getVariableIds().containsAll(
                Set.of("oval:t:var:1", "oval:t:var:2", "oval:t:var:3")));
        // var:3 has no definition in the index
        assertTrue(closure.getUnsupportedVariableReasons().containsKey("oval:t:var:3"));
    }

    @Test
    void resolve_shouldCollectVariablesFromFilterOnlyStates() {
        OvalIndex index = new OvalIndex();
        index.getStateById().put("oval:t:ste:5", stateWithVarRef("oval:t:ste:5", "oval:t:var:7"));
        index.getVariableById().put("oval:t:var:7",
                localVariable("oval:t:var:7", objectComponent("oval:t:obj:99")));

        ParsedOvalObject filtered = objectWithVarRef("oval:t:obj:1", "name", null);
        ParsedOvalFilter filter = new ParsedOvalFilter();
        filter.setAction("include");
        filter.setStateRef("oval:t:ste:5");
        filtered.getFilters().add(filter);

        ResolvedOvalEvaluationSlice slice = slice(List.of(filtered), List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getVariableIds().contains("oval:t:var:7"));
        assertTrue(closure.getObjectIds().contains("oval:t:obj:99"));
    }

    @Test
    void resolve_shouldDetectCircularVariableReferences() {
        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:1",
                localVariable("oval:t:var:1", variableComponent("oval:t:var:2")));
        index.getVariableById().put("oval:t:var:2",
                localVariable("oval:t:var:2", variableComponent("oval:t:var:1")));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(objectWithVarRef("oval:t:obj:1", "name", "oval:t:var:1")),
                List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getUnsupportedVariableReasons().keySet().stream()
                .anyMatch(Set.of("oval:t:var:1", "oval:t:var:2")::contains));
    }

    @Test
    void resolve_shouldMarkUnsupportedFunctionVariables() {
        ParsedUnsupportedComponent split = new ParsedUnsupportedComponent();
        split.setFunctionName("split");

        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:1", localVariable("oval:t:var:1", split));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(objectWithVarRef("oval:t:obj:1", "name", "oval:t:var:1")),
                List.of());

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getUnsupportedVariableReasons().get("oval:t:var:1").contains("split"));
    }

    @Test
    void resolve_shouldCollectVariablesFromSliceStates() {
        OvalIndex index = new OvalIndex();
        index.getVariableById().put("oval:t:var:4",
                localVariable("oval:t:var:4", objectComponent("oval:t:obj:50")));

        ResolvedOvalEvaluationSlice slice = slice(
                List.of(),
                List.of(stateWithVarRef("oval:t:ste:1", "oval:t:var:4")));

        ResolvedOvalVariableClosure closure = resolver.resolve(index, slice);

        assertTrue(closure.getVariableIds().contains("oval:t:var:4"));
        assertTrue(closure.getObjectIds().contains("oval:t:obj:50"));
    }

    private ResolvedOvalEvaluationSlice slice(
            final List<ParsedOvalObject> objects,
            final List<ParsedOvalState> states) {
        return new ResolvedOvalEvaluationSlice(
                List.of(), List.of(), List.copyOf(objects), List.copyOf(states));
    }

    private ParsedOvalObject objectWithVarRef(final String objectId, final String entityName, final String varRef) {
        ParsedOvalObject object = new ParsedOvalObject();
        object.setObjectId(objectId);
        object.setObjectType("registry_object");

        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName(entityName);
        if (varRef != null) {
            entity.getAttributes().put("var_ref", varRef);
        } else {
            entity.setValue("literal-value");
        }
        object.getEntities().add(entity);
        return object;
    }

    private ParsedOvalState stateWithVarRef(final String stateId, final String varRef) {
        ParsedOvalState state = new ParsedOvalState();
        state.setStateId(stateId);
        state.setStateType("registry_state");

        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName("value");
        entity.getAttributes().put("var_ref", varRef);
        state.getEntities().add(entity);
        return state;
    }

    private ParsedOvalLocalVariable localVariable(final String id, final ParsedOvalVariableComponent expression) {
        ParsedOvalLocalVariable variable = new ParsedOvalLocalVariable();
        variable.setId(id);
        variable.setDatatype("string");
        variable.setExpression(expression);
        return variable;
    }

    private ParsedVariableComponent variableComponent(final String varRef) {
        ParsedVariableComponent component = new ParsedVariableComponent();
        component.setVarRef(varRef);
        return component;
    }

    private ParsedObjectComponent objectComponent(final String objectRef) {
        ParsedObjectComponent component = new ParsedObjectComponent();
        component.setObjectRef(objectRef);
        component.setItemField("value");
        return component;
    }
}