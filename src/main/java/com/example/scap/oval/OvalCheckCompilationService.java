package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.variables.ResolvedVariableBindings;

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