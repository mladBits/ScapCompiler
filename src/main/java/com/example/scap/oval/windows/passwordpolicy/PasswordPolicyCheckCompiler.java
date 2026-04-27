package com.example.scap.oval.windows.passwordpolicy;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PasswordPolicyCheckCompiler extends CheckCompilerBase<CompiledPasswordPolicyCheck> {
    @Override
    protected String supportedTestType() {
        return "passwordpolicy_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(new PasswordPolicyCollectionTask())))
                        .build());
    }

    @Override
    protected Optional<CompiledPasswordPolicyCheck> compileResolved(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test,
            final List<ParsedOvalState> states,
            final ObjectCompilationResult objectResult) {

        final CompiledPasswordPolicyCheck check = new CompiledPasswordPolicyCheck();
        check.setTestId(test.getId());
        return Optional.of(check);
    }
}
