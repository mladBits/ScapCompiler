package com.touchstone.compiler.model.normalized.oval;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class VariableValueExpression implements OvalValueExpression {
    private String variableId;
}
