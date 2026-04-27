package com.example.scap.model.compiled.variables;

import lombok.Data;

@Data
public class CompiledLocalVariableExpression {
    private String variableId;
    private String datatype;
    private CompiledVariableExpression expression;
}
