package com.example.scap.oval.windows.passwordpolicy;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordPolicyCheckCompiler extends CheckCompilerBase {
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
}
