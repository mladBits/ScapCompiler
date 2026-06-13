package com.touchstone.compiler.model.compiled.variables;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The OVAL variable type (origin). Whether a variable is actually usable is a
 * separate concern: see {@code CompiledVariable.unresolved}.
 */
public enum CompiledVariableKind {
    /** constant_variable: fixed values defined in the OVAL. */
    @JsonProperty("constant")
    CONSTANT,

    /** external_variable: value supplied from outside (XCCDF). Carries values when bound. */
    @JsonProperty("external")
    EXTERNAL,

    /** local_variable: computed at runtime from the expression tree. */
    @JsonProperty("local")
    LOCAL
}
