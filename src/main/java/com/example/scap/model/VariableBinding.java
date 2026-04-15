package com.example.scap.model;

public record VariableBinding(
        String variableId,
        String value,
        String source
) {
}
