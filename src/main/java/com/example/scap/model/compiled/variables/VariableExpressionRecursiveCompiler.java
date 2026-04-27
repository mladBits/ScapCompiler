package com.example.scap.model.compiled.variables;

import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;

@FunctionalInterface
public interface VariableExpressionRecursiveCompiler {
    CompiledVariableExpression compile(
            LocalVariableCompileContext context,
            ParsedOvalVariableComponent component
    );
}
