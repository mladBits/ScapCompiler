package com.touchstone.compiler.model.compiled.variables;

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

    /** OVAL variable type (constant / external / local). */
    private CompiledVariableKind kind;

    /**
     * When true the agent cannot resolve this variable (unbound external,
     * unsupported local function, or cycle); dependent tests evaluate to error.
     * Omitted (false) otherwise.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean unresolved;

    /** Why the variable is unresolved (diagnostic); present only when unresolved. */
    private String unresolvedReason;

    /** Resolved values: present for constant variables and bound externals. */
    private List<String> values;

    /** Runtime evaluation tree: present only for local variables. */
    private CompiledVariableExpression expression;
}