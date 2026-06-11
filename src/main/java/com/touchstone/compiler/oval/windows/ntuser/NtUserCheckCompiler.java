package com.touchstone.compiler.oval.windows.ntuser;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.EntitySelector;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NtUserCheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() {
        return "ntuser_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final EntitySelector key = object.findEntity("key")
                .orElseThrow(() -> new IllegalArgumentException("Missing key"))
                .resolve();

        final EntitySelector name = object.findEntity("name")
                .orElseThrow(() -> new IllegalArgumentException("Missing name"))
                .resolve();

        final NtUserCollectionTask task = new NtUserCollectionTask();
        task.addSelector(key);
        task.addSelector(name);

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
