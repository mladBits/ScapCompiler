package com.example.scap.variables;

public sealed interface VariableAwareEntityValue
        permits BoundVariableReference,
                LiteralEntityValue,
                RuntimeVariableReference,
                UnresolvedVariableReference {
}