package com.example.scap.port;

import com.example.scap.model.PolicyAssignment;

import java.util.Optional;

public interface AssignmentRepositoryPort {
    void save(PolicyAssignment assignment);
    Optional<PolicyAssignment> findById(String assignmentId);
}