package com.example.scap.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CompileTemplateRequest(
        @NotBlank String benchmarkId,
        @NotBlank String profileId,
        @NotBlank String contentVersion,
        @NotBlank String xccdfStorageKey,
        @NotBlank String ovalStorageKey
) {
}