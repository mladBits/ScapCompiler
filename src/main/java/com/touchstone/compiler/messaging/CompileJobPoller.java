package com.touchstone.compiler.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.api.dto.CompileTemplateResponse;
import com.touchstone.compiler.config.AwsProperties;
import com.touchstone.compiler.service.TemplateCompileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Long-polls the compile-jobs queue and runs each job through
 * {@link TemplateCompileService}. Message body is a JSON
 * {@link CompileTemplateRequest}: {packageId, profileIds?} — profileIds
 * null/empty means every profile in the package.
 *
 * Failed jobs are NOT deleted: SQS redelivers after the visibility timeout
 * and the queue's redrive policy dead-letters them after 3 attempts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.compile-job-poller.enabled", havingValue = "true")
public class CompileJobPoller implements SmartLifecycle {

    private static final int WAIT_TIME_SECONDS = 20;
    private static final int MAX_MESSAGES_PER_POLL = 5;
    private static final Duration POLL_FAILURE_BACKOFF = Duration.ofSeconds(5);

    private final SqsClient sqsClient;
    private final AwsProperties properties;
    private final TemplateCompileService templateCompileService;
    private final ObjectMapper objectMapper;

    private volatile boolean running;
    private ExecutorService executor;

    @Override
    public void start() {
        running = true;
        executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "compile-job-poller");
            thread.setDaemon(true);
            return thread;
        });
        executor.submit(this::pollLoop);
        log.info("Compile job poller started on {}", properties.compileQueueUrl());
    }

    private void pollLoop() {
        while (running) {
            final List<Message> messages;
            try {
                messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                                .queueUrl(properties.compileQueueUrl())
                                .waitTimeSeconds(WAIT_TIME_SECONDS)
                                .maxNumberOfMessages(MAX_MESSAGES_PER_POLL)
                                .build())
                        .messages();
            } catch (final Exception e) {
                if (running) {
                    log.error("Compile queue poll failed; retrying in {}s",
                            POLL_FAILURE_BACKOFF.toSeconds(), e);
                    sleep(POLL_FAILURE_BACKOFF);
                }
                continue;
            }

            for (final Message message : messages) {
                if (!running) {
                    return;
                }
                handle(message);
            }
        }
    }

    // Package-private for tests.
    void handle(final Message message) {
        final CompileTemplateRequest request;
        try {
            request = objectMapper.readValue(message.body(), CompileTemplateRequest.class);
        } catch (final Exception e) {
            // Unparseable now means unparseable on retry too; leave the
            // message so the redrive policy parks it in the DLQ for triage.
            log.error("Unparseable compile job message {}: {}", message.messageId(), message.body(), e);
            return;
        }

        try {
            final List<CompileTemplateResponse> responses = templateCompileService.compile(request);
            for (final CompileTemplateResponse response : responses) {
                log.info("Compiled {} / {} -> {}",
                        request.getPackageId(), response.getProfileId(), response.getArtifactLocation());
            }
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(properties.compileQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (final Exception e) {
            log.error("Compile job failed for {} / {}; message will be retried, then dead-lettered",
                    request.getPackageId(), request.getProfileIds(), e);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Compile job poller stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private static void sleep(final Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
