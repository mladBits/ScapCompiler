package com.touchstone.compiler.oval.windows.usersid55;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserSid55CheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() {
        return "user_sid55_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final UserSid55CollectionTask task = new UserSid55CollectionTask();

        task.addSelector(object.findEntity("user_sid")
                .orElseThrow(() -> new IllegalArgumentException("Missing user_sid"))
                .resolve());

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
