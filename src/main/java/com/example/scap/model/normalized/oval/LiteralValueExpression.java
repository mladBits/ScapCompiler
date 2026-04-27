package com.example.scap.model.normalized.oval;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class LiteralValueExpression implements OvalValueExpression {
    private String value;
}