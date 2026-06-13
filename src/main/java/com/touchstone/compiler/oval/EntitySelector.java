package com.touchstone.compiler.oval;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EntitySelector {
    private String field;
    private String operation;
    private String datatype;

    /**
     * How a multi-valued variable folds against an item value
     * (all / at least one / none satisfy / only one).
     * Only present when the expression references a variable.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String varCheck;

    /**
     * Exactly one of expression/fields is present: scalar entities carry an
     * expression; record-datatype entities carry nested field assertions.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Expression expression;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<EntitySelector> fields = new ArrayList<>();

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
