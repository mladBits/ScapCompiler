package com.example.scap.variables;

import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.normalized.oval.OvalEntityConstraint;

public interface VariableAwareEntityResolver {
    VariableAwareEntityValue resolve(
            OvalEntityConstraint constraint,
            ResolvedVariableBindings bindings,
            LocalVariableCompilationResult localVariables
    );
}
