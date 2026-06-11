package com.touchstone.compiler.variables;

public sealed interface VariableAwareEntityValue
        permits BoundVariableReference,
                LiteralEntityValue,
                RuntimeVariableReference,
                UnresolvedVariableReference {
}