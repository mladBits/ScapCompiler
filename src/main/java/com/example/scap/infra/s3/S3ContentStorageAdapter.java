package com.example.scap.infra.s3;

import com.example.scap.config.AwsProperties;
import com.example.scap.port.ContentStoragePort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3ContentStorageAdapter implements ContentStoragePort {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public S3ContentStorageAdapter(final S3Client s3Client, final AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    @Override
    public String put(final String key, byte[] bytes, final String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(awsProperties.rawContentBucket())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes)
        );
        return key;
    }

    @Override
    public byte[] get(final String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(awsProperties.rawContentBucket())
                        .key(key)
                        .build()
        ).asByteArray();
    }
}
