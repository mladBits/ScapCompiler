package com.example.scap.model.compiled.variables;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A variable entry in the execution template. The agent resolves variables
 * lazily and memoizes results; see contracts/variable-resolution.md.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompiledVariable {
    private String variableId;
    private String datatype;
    private CompiledVariableKind kind;

    /**
     * Resolved values, present only when kind == LITERAL.
     */
    private List<String> values;

    /**
     * Runtime evaluation plan, present only when kind == PLAN.
     */
    private CompiledVariableExpression expression;
}