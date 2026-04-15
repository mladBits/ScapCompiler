package com.example.scap.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateAssignmentRequest(
        @NotBlank String benchmarkId,
        @NotBlank String profileId,
        @NotBlank String contentVersion,
        @NotEmpty List<@Valid VariableBindingRequest> bindings
) {
    public record VariableBindingRequest(
            @NotBlank String variableId,
            @NotBlank String value
    ) {
    }
}
