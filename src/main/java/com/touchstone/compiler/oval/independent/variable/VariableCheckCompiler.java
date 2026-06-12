package com.touchstone.compiler.oval.independent.variable;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OVAL independent-family variable_test: evaluates a variable's resolved
 * value against a state, without touching the host. The variable id is the
 * text value of the object's var_ref entity (not a var_ref attribute);
 * OvalVariableClosureResolver knows this convention too, so the variable is
 * guaranteed to be materialized in variablesById.
 */
@Component
public class VariableCheckCompiler extends CheckCompilerBase {

    @Override
    protected String supportedTestType() {
        return "variable_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final String variableId = object.findEntity("var_ref")
                .orElseThrow(() -> new IllegalArgumentException("Missing var_ref"))
                .getValue();
        if (variableId == null || variableId.isBlank()) {
            throw new IllegalArgumentException("Empty var_ref in " + object.getObjectId());
        }

        final VariableCollectionTask task = new VariableCollectionTask(variableId);

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
