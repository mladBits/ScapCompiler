package com.example.scap.model.parsed.xccdf;

import java.util.List;

public record ParsedXccdfValue(
        String valueId,
        String title,
        String type,
        String defaultValue,
        List<String> allowedValues,
        Integer minInt,
        Integer maxInt
) {
}
