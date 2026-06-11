package com.touchstone.compiler.model.compiled.variables;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class LocalVariableCompilationResult {
    private final Map<String, CompiledLocalVariableExpression> localVariablesById = new LinkedHashMap<>();

    /**
     * Local variables whose plan could not be compiled (e.g. an unsupported
     * function), keyed by variable id with the failure reason. These are
     * emitted as UNRESOLVED in the template instead of failing the compile.
     */
    private final Map<String, String> unsupportedVariableReasons = new LinkedHashMap<>();
}