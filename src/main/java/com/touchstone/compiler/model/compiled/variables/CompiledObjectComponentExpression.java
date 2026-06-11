package com.touchstone.compiler.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CompiledObjectComponentExpression implements CompiledVariableExpression {
    private String objectRef;
    private String itemField;
}
