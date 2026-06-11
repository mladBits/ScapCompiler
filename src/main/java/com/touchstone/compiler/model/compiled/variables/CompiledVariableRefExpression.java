package com.touchstone.compiler.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CompiledVariableRefExpression implements CompiledVariableExpression {
    private String variableId;
}