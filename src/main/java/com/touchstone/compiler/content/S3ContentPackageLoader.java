package com.touchstone.compiler.content;

import com.touchstone.compiler.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.InputStream;

/**
 * Loads content packages from the raw-content bucket. The key layout is the
 * contract with the infra seed script (touchstone-infra/scripts/seed-content.ps1):
 * packages/&lt;benchmarkId&gt;/xccdf.xml and packages/&lt;benchmarkId&gt;/oval.xml.
 */
@Component
@RequiredArgsConstructor
public class S3ContentPackageLoader implements ContentPackageLoader {

    private final S3Client s3Client;
    private final AwsProperties properties;

    @Override
    public ContentPackage load(final String packageId) {
        return new ContentPackage(
                open(packageId, "xccdf.xml"),
                open(packageId, "oval.xml"));
    }

    private InputStream open(final String packageId, final String fileName) {
        final String key = "packages/" + packageId + "/" + fileName;
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.rawContentBucket())
                    .key(key)
                    .build());
        } catch (final NoSuchKeyException e) {
            throw new IllegalArgumentException("Content package not found: s3://"
                    + properties.rawContentBucket() + "/" + key, e);
        }
    }
}
