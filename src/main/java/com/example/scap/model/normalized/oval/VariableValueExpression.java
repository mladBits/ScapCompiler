package com.example.scap.model.normalized.oval;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class VariableValueExpression implements OvalValueExpression {
    private String variableId;
}
