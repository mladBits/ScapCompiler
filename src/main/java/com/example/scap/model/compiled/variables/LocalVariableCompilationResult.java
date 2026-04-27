package com.example.scap.model.compiled.variables;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class LocalVariableCompilationResult {
    private final Map<String, CompiledLocalVariableExpression> localVariablesById = new LinkedHashMap<>();
}
