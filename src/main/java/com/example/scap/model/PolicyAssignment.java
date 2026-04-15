package com.example.scap.model;

import java.time.Instant;
import java.util.List;

public record PolicyAssignment(
        String assignmentId,
        TemplateKey templateKey,
        List<VariableBinding> bindings,
        Instant createdAt
) {
}
