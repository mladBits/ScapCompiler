package com.touchstone.compiler.oval.windows.accesstoken;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.EntitySelector;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccessTokenCheckCompiler extends CheckCompilerBase  {
    @Override
    protected String supportedTestType() {
        return "accesstoken_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final EntitySelector securityPrinciple = object.findEntity("security_principle")
                .orElseThrow(() -> new IllegalArgumentException("Missing security_principle"))
                .resolve();

        final AccessTokenCollectionTask task = new AccessTokenCollectionTask();
        task.addSelector(securityPrinciple);

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
