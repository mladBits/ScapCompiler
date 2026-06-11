package com.touchstone.compiler.model.compiled.variables;

import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;

@FunctionalInterface
public interface VariableExpressionRecursiveCompiler {
    CompiledVariableExpression compile(
            LocalVariableCompileContext context,
            ParsedOvalVariableComponent component
    );
}
