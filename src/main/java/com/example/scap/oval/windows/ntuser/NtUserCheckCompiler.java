package com.example.scap.oval.windows.ntuser;

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
public class NtUserCheckCompiler extends CheckCompilerBase<CompiledNtUserCheck> {
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

    @Override
    protected Optional<CompiledNtUserCheck> compileResolved(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test,
            final List<ParsedOvalState> states,
            final ObjectCompilationResult objectResult) {
        final CompiledNtUserCheck check = new CompiledNtUserCheck();
        check.setTestId(test.getId());
        check.setObjectId(test.getObjectRef());
        check.setCheck(test.getCheck());
        check.setCheckExistence(test.getCheckExistence());

        return Optional.of(check);
    }
}
