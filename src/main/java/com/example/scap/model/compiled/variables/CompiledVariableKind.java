package com.example.scap.model.compiled.variables;

public enum CompiledVariableKind {
    /**
     * Values were fully resolved at compile time (external/constant variables).
     */
    LITERAL,

    /**
     * A local variable the agent evaluates at runtime from the expression plan.
     */
    PLAN,

    /**
     * The variable could not be resolved or compiled. Tests depending on it
     * must evaluate to error per the OVAL specification.
     */
    UNRESOLVED
}