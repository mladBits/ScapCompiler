package com.example.scap.service;

import com.example.scap.model.ContentPackage;
import com.example.scap.port.ContentStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentIngestService {
    private final ContentStoragePort contentStoragePort;

    public ContentPackage ingest(List<MultipartFile> files) {
        String packageId = UUID.randomUUID().toString();

        List<String> keys = files.stream()
                .map(file -> {
                    try {
                        String key = "packages/%s/%s".formatted(packageId, file.getOriginalFilename());
                        return contentStoragePort.put(key, file.getBytes(), file.getContentType());
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to store file " + file.getOriginalFilename(), e);
                    }
                })
                .toList();

        return new ContentPackage(packageId, "TBD", keys, Instant.now());
    }
}
