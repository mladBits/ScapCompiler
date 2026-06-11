package com.touchstone.compiler.model.compiled.variables;

import lombok.Data;

@Data
public class CompiledLocalVariableExpression {
    private String variableId;
    private String datatype;
    private CompiledVariableExpression expression;
}
