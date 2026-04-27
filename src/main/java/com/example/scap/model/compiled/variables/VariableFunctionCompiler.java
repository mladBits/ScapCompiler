package com.example.scap.model.compiled.variables;

import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;

public interface VariableFunctionCompiler<T extends ParsedOvalVariableComponent> {
    boolean supports(ParsedOvalVariableComponent component);
    CompiledVariableExpression compile(
            LocalVariableCompileContext context,
            T component
    );
}
