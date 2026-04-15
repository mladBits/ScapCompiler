package com.example.scap.service;

import com.example.scap.model.PolicyAssignment;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableBinding;
import com.example.scap.port.AssignmentRepositoryPort;
import com.example.scap.port.TemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyAssignmentService {
    private final TemplateRepositoryPort templateRepositoryPort;
    private final AssignmentRepositoryPort assignmentRepositoryPort;

    public PolicyAssignment create(
            final TemplateKey key,
            final List<VariableBinding> bindings
    ) {
        templateRepositoryPort.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        final PolicyAssignment assignment = new PolicyAssignment(
                UUID.randomUUID().toString(),
                key,
                bindings,
                Instant.now()
        );

        assignmentRepositoryPort.save(assignment);
        return assignment;
    }
}
