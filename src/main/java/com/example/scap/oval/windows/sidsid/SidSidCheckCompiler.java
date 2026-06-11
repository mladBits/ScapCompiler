package com.example.scap.oval.windows.sidsid;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.common.CheckCompilerBase;
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