package com.touchstone.compiler.oval.windows.sidsid;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SidSidCheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() {
        return "sid_sid_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final SidSidCollectionTask task = new SidSidCollectionTask();

        task.addSelector(object.findEntity("trustee_sid")
                .orElseThrow(() -> new IllegalArgumentException("Missing trustee_sid"))
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