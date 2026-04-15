package com.example.scap.infra.sqs;

import com.example.scap.config.AwsProperties;
import com.example.scap.port.QueuePublisherPort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsQueuePublisherAdapter implements QueuePublisherPort {

    private final SqsClient sqsClient;
    private final AwsProperties awsProperties;

    public SqsQueuePublisherAdapter(
            final SqsClient sqsClient,
            final AwsProperties awsProperties) {
        this.sqsClient = sqsClient;
        this.awsProperties = awsProperties;
    }

    @Override
    public void publish(final String payload) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(awsProperties.compileQueueUrl())
                .messageBody(payload)
                .build());
    }
}
