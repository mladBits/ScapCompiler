package com.example.scap.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CompiledVariableRefExpression implements CompiledVariableExpression {
    private String variableId;
}