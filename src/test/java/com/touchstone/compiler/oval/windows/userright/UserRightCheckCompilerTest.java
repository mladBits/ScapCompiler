package com.touchstone.compiler.oval.windows.userright;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalEntity;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.EntitySelector;
import com.touchstone.compiler.oval.OvalCheckCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompilationServiceImpl;
import com.touchstone.compiler.variables.ResolvedVariableBindings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRightCheckCompilerTest {

    private final OvalCheckCompilationServiceImpl service =
            new OvalCheckCompilationServiceImpl(List.of(new UserRightCheckCompiler()));

    @Test
    void compile_shouldEmitUserRightTaskAndTrusteeState() {
        ParsedOvalObject object = new ParsedOvalObject();
        object.setObjectId("oval:t:obj:1");
        object.setObjectType("userright_object");
        ParsedOvalEntity userright = new ParsedOvalEntity();
        userright.setName("userright");
        userright.setValue("SE_NETWORK_LOGON_NAME");
        userright.getAttributes().put("operation", "equals");
        object.getEntities().add(userright);

        ParsedOvalState state = new ParsedOvalState();
        state.setStateId("oval:t:ste:1");
        state.setStateType("userright_state");
        ParsedOvalEntity trusteeSid = new ParsedOvalEntity();
        trusteeSid.setName("trustee_sid");
        trusteeSid.setValue("S-1-5-32-(544|555)");
        trusteeSid.getAttributes().put("operation", "pattern match");
        state.getEntities().add(trusteeSid);

        ParsedOvalTest test = new ParsedOvalTest();
        test.setId("oval:t:tst:1");
        test.setTestType("userright_test");
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
        UserRightCollectionTask task = (UserRightCollectionTask) plan.getTasks().getFirst();
        assertEquals("windows.userright", task.getFamily());
        EntitySelector selector = task.getSelectors().getFirst();
        assertEquals("userright", selector.getField());
        assertTrue(String.valueOf((Object) selector.getExpression()).contains("SE_NETWORK_LOGON_NAME"));

        assertEquals("pattern match",
                result.getStates().get("oval:t:ste:1").getAssertions().getFirst().getOperation());
    }
}
