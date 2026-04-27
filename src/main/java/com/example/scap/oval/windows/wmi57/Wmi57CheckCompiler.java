package com.example.scap.oval.windows.wmi57;

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
public class Wmi57CheckCompiler extends CheckCompilerBase<CompiledWmi57Check> {
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

    @Override
    protected Optional<CompiledWmi57Check> compileResolved(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test,
            final List<ParsedOvalState> states,
            final ObjectCompilationResult objectResult) {

        final CompiledWmi57Check check = new CompiledWmi57Check();
        check.setTestId(test.getId());
        return Optional.of(check);
    }
}

