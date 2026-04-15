package com.example.scap.model;

public record TemplateKey(
        String benchmarkId,
        String profileId,
        String contentVersion
) {
}
