package com.touchstone.compiler.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public final class CompiledLiteralExpression implements CompiledVariableExpression {
    private String value;
}
