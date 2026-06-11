package com.touchstone.compiler.oval.windows.wmi57;

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
public class Wmi57CheckCompiler extends CheckCompilerBase {
    private static final String NAMESPACE_ENTITY = "namespace";
    private static final String WQL_ENTITY = "wql";

    @Override
    protected String supportedTestType() {
        return "wmi57_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final EntitySelector namespace = object.findEntity(NAMESPACE_ENTITY)
                .orElseThrow(() -> new IllegalArgumentException("Missing namespace"))
                .resolve();

        final EntitySelector wql = object.findEntity(WQL_ENTITY)
                .orElseThrow(() -> new IllegalArgumentException("Missing wql"))
                .resolve();

        final Wmi57CollectionTask wmi57CollectionTask = new Wmi57CollectionTask();
        wmi57CollectionTask.addSelector(namespace);
        wmi57CollectionTask.addSelector(wql);

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(wmi57CollectionTask)))
                        .build());
    }
}

