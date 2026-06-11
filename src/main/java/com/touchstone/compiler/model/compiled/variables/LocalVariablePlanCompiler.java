package com.touchstone.compiler.model.compiled.variables;

import com.touchstone.compiler.index.OvalIndex;

import java.util.Collection;

public interface LocalVariablePlanCompiler {
    LocalVariableCompilationResult compile(
            OvalIndex ovalIndex,
            Collection<String> variableIds
    );
}
