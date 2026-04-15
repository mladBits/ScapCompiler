package com.example.scap.infra.memory;

import com.example.scap.model.PolicyAssignment;
import com.example.scap.port.AssignmentRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAssignmentRepository implements AssignmentRepositoryPort {

    private final Map<String, PolicyAssignment> store = new ConcurrentHashMap<>();

    @Override
    public void save(final PolicyAssignment assignment) {
        store.put(assignment.assignmentId(), assignment);
    }

    @Override
    public Optional<PolicyAssignment> findById(final String assignmentId) {
        return Optional.ofNullable(store.get(assignmentId));
    }
}
