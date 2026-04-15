package com.example.scap.model;

import java.time.Instant;
import java.util.List;

public record ContentPackage(
        String packageId,
        String contentVersion,
        List<String> fileKeys,
        Instant createdAt
) {
}
