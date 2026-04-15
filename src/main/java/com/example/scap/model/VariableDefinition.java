package com.example.scap.model;

import java.util.List;

public record VariableDefinition(
        String id,
        String title,
        String type,
        boolean required,
        boolean multiValued,
        String defaultValue,
        List<String> allowedValues,
        Integer minInt,
        Integer maxInt
) {
}