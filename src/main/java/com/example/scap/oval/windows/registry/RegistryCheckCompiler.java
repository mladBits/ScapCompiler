package com.example.scap.oval.windows.registry;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.EntitySelector;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.common.CheckCompilerBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegistryCheckCompiler extends CheckCompilerBase<CompiledRegistryCheck> {

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

    @Override
    protected Optional<CompiledRegistryCheck> compileResolved(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test,
            final List<ParsedOvalState> states,
            final ObjectCompilationResult objectResult) {

        final CompiledRegistryCheck check = new CompiledRegistryCheck();
        check.setTestId(test.getId());
        check.setObjectId(test.getObjectRef());
        check.setCheck(test.getCheck());
        check.setCheckExistence(test.getCheckExistence());

        return Optional.of(check);
    }
}
