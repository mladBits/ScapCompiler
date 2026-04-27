package com.example.scap.oval;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class EntitySelector {
    private String field;
    private String operator;
    private String datatype;
    private Expression expression;

    public void setValue(final String type, final String value) {
        expression = new Expression(type, value);
    }

    @Data
    @AllArgsConstructor
    private static final class Expression {
        private String type;
        private String value;
    }
}
