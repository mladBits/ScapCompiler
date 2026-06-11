package com.touchstone.compiler.model.normalized.oval;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class LiteralValueExpression implements OvalValueExpression {
    private String value;
}