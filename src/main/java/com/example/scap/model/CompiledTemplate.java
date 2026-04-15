package com.example.scap.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CompiledTemplate(
        TemplateKey key,
        List<VariableDefinition> variableCatalog,
        Map<String, Object> executionPlan,
        Instant generatedAt
) {
}
