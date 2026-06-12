package com.touchstone.compiler.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.touchstone.compiler.config.AwsProperties;
import com.touchstone.compiler.model.compiled.ExecutionTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Writes compiled templates to the compiled-template bucket under a
 * deterministic key, so re-compiling a benchmark/profile pair overwrites the
 * previous artifact (latest-wins until template versioning lands).
 */
@Component
@RequiredArgsConstructor
public class S3ExecutionTemplateStore implements ExecutionTemplateStore {

    private final S3Client s3Client;
    private final AwsProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public String store(final ExecutionTemplate template) {
        // Keyed by packageId (not the XCCDF benchmarkId) so template locations
        // are predictable from the raw-content package key alone, without
        // parsing the content.
        final String key = "templates/"
                + sanitizeKeySegment(template.getPackageId()) + "/"
                + sanitizeKeySegment(template.getProfileId()) + ".json";

        final byte[] body;
        try {
            body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(template);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize template " + template.getTemplateId(), e);
        }

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.compiledTemplateBucket())
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromBytes(body));

        return "s3://" + properties.compiledTemplateBucket() + "/" + key;
    }

    /**
     * XCCDF profile ids may contain '/', spaces, and parentheses (CIS does
     * this); keep key segments to a safe character set.
     */
    private static String sanitizeKeySegment(final String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
