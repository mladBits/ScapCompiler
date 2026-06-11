package com.touchstone.compiler.variables;

import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.normalized.oval.OvalEntityConstraint;

public interface VariableAwareEntityResolver {
    VariableAwareEntityValue resolve(
            OvalEntityConstraint constraint,
            ResolvedVariableBindings bindings,
            LocalVariableCompilationResult localVariables
    );
}
