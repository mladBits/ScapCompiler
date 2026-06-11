package com.touchstone.compiler.model.normalized.oval;

public sealed interface OvalValueExpression
    permits LiteralValueExpression, VariableValueExpression {
}
