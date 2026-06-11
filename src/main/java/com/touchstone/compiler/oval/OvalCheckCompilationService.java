package com.touchstone.compiler.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.variables.ResolvedVariableBindings;

import java.util.Collection;

public interface OvalCheckCompilationService {
    OvalCheckCompilationResult compile(
            OvalIndex ovalIndex,
            ResolvedOvalEvaluationSlice slice,
            ResolvedVariableBindings bindings,
            LocalVariableCompilationResult localVariables,
            Collection<String> variableReferencedObjectIds
    );
}