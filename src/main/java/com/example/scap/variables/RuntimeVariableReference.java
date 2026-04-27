package com.example.scap.variables;

import com.example.scap.model.compiled.variables.CompiledLocalVariableExpression;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class RuntimeVariableReference implements VariableAwareEntityValue {
    private String variableId;
    private CompiledLocalVariableExpression expression;
}