package com.touchstone.compiler.model.compiled.variables;

import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;

public interface VariableFunctionCompiler<T extends ParsedOvalVariableComponent> {
    boolean supports(ParsedOvalVariableComponent component);
    CompiledVariableExpression compile(
            LocalVariableCompileContext context,
            T component
    );
}
