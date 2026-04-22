package com.example.scap.model.normalized.oval;

public sealed interface OvalValueExpression
    permits LiteralValueExpression, VariableValueExpression {
}
