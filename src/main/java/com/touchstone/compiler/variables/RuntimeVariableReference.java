package com.touchstone.compiler.variables;

import com.touchstone.compiler.model.compiled.variables.CompiledLocalVariableExpression;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class RuntimeVariableReference implements VariableAwareEntityValue {
    private String variableId;
    private CompiledLocalVariableExpression expression;
}