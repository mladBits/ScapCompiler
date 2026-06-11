package com.touchstone.compiler.oval.windows.registry;

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
public class RegistryCheckCompiler extends CheckCompilerBase {

    @Override
    protected String supportedTestType() {
        return "registry_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final EntitySelector hive = object.findEntity("hive")
                .orElseThrow(() -> new IllegalArgumentException("Missing hive"))
                .resolve();

        final EntitySelector key = object.findEntity("key")
                .orElseThrow(() -> new IllegalArgumentException("Missing key"))
                .resolve();

        final EntitySelector name = object.findEntity("name")
                .orElseThrow(() -> new IllegalArgumentException("Missing name"))
                .resolve();

        final RegistryCollectionTask task = new RegistryCollectionTask();
        task.addSelector(hive);
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
