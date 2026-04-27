package com.example.scap.model.compiled.variables;

import com.example.scap.index.OvalIndex;

import java.util.Collection;

public interface LocalVariablePlanCompiler {
    LocalVariableCompilationResult compile(
            OvalIndex ovalIndex,
            Collection<String> variableIds
    );
}
