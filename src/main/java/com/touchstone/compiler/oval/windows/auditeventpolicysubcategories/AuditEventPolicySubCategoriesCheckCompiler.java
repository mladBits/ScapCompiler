package com.touchstone.compiler.oval.windows.auditeventpolicysubcategories;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuditEventPolicySubCategoriesCheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() {
        return "auditeventpolicysubcategories_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        // The object carries no entities: the probe collects the full audit
        // subcategory policy and the states assert individual subcategories.
        final AuditEventPolicySubCategoriesCollectionTask task =
                new AuditEventPolicySubCategoriesCollectionTask();

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
