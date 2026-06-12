package com.touchstone.compiler.oval.independent.variable;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalEntity;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.OvalCheckCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompilationServiceImpl;
import com.touchstone.compiler.variables.ResolvedVariableBindings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableCheckCompilerTest {

    private final OvalCheckCompilationServiceImpl service =
            new OvalCheckCompilationServiceImpl(List.of(new VariableCheckCompiler()));

    @Test
    void compile_shouldEmitVariableTaskCarryingTheVariableId() {
        ParsedOvalObject object = variableObject("oval:t:obj:1", "oval:t:var:104");

        ParsedOvalState state = new ParsedOvalState();
        state.setStateId("oval:t:ste:1");
        state.setStateType("variable_state");
        ParsedOvalEntity value = new ParsedOvalEntity();
        value.setName("value");
        value.setValue("^[yY]$");
        value.getAttributes().put("operation", "pattern match");
        state.getEntities().add(value);

        ParsedOvalTest test = new ParsedOvalTest();
        test.setId("oval:t:tst:1");
        test.setTestType("variable_test");
        test.setObjectRef("oval:t:obj:1");
        test.getStateRef().add("oval:t:ste:1");
        test.setCheck("all");
        test.setCheckExistence("at_least_one_exists");

        OvalIndex index = new OvalIndex();
        index.getObjectById().put(object.getObjectId(), object);
        index.getStateById().put(state.getStateId(), state);

        OvalCheckCompilationResult result = service.compile(
                index,
                new ResolvedOvalEvaluationSlice(List.of(), List.of(test), List.of(object), List.of(state)),
                new ResolvedVariableBindings(),
                new LocalVariableCompilationResult(),
                List.of());

        assertTrue(result.getUnsupportedCheckTypes().isEmpty());
        assertEquals(1, result.getCompiledChecks().size());

        CompiledObjectPlan plan = result.getObjects().get("oval:t:obj:1");
        VariableCollectionTask task = (VariableCollectionTask) plan.getTasks().getFirst();
        assertEquals("independent.variable", task.getFamily());
        assertEquals("oval:t:var:104", task.getVariableId());
    }

    @Test
    void compileSimpleObject_shouldRejectEmptyVarRef() {
        VariableCheckCompiler compiler = new VariableCheckCompiler();
        ParsedOvalObject object = variableObject("oval:t:obj:2", "");

        assertThrows(IllegalArgumentException.class,
                () -> compiler.compileObjectPlan(null, object));
    }

    private ParsedOvalObject variableObject(final String objectId, final String varRefValue) {
        ParsedOvalObject object = new ParsedOvalObject();
        object.setObjectId(objectId);
        object.setObjectType("variable_object");
        ParsedOvalEntity varRef = new ParsedOvalEntity();
        varRef.setName("var_ref");
        varRef.setValue(varRefValue);
        object.getEntities().add(varRef);
        return object;
    }
}
