package com.example.scap.api;

import com.example.scap.api.dto.CreateAssignmentRequest;
import com.example.scap.model.PolicyAssignment;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableBinding;
import com.example.scap.service.PolicyAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final PolicyAssignmentService policyAssignmentService;

    @PostMapping
    public PolicyAssignment create(@Valid @RequestBody CreateAssignmentRequest request) {
        TemplateKey key = new TemplateKey(
                request.benchmarkId(),
                request.profileId(),
                request.contentVersion()
        );

        List<VariableBinding> bindings = request.bindings().stream()
                .map(b -> new VariableBinding(b.variableId(), b.value(), "USER_INPUT"))
                .toList();

        return policyAssignmentService.create(key, bindings);
    }
}