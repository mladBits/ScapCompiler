package com.touchstone.compiler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AwsProperties(
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        String rawContentBucket,
        String compiledTemplateBucket,
        String compileQueueUrl
) {
}
