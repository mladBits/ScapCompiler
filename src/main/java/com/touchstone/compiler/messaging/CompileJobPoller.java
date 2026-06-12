package com.touchstone.compiler.messaging;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Long-polls the compile-jobs queue and runs each job through
 * {@link TemplateCompileService}. Two message shapes are accepted:
 *
 * 1. A JSON {@link CompileTemplateRequest}: {packageId, profileIds?} —
 *    profileIds null/empty means every profile in the package.
 * 2. An S3 event notification (the raw-content bucket notifies this queue on
 *    package uploads): each record whose key is packages/&lt;id&gt;/oval.xml
 *    becomes an all-profiles compile of that package. S3's TestEvent
 *    handshake message is acknowledged and dropped.
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

    private static final Pattern PACKAGE_UPLOAD_KEY =
            Pattern.compile("^packages/([^/]+)/oval\\.xml$");

    // Package-private for tests.
    void handle(final Message message) {
        final List<CompileTemplateRequest> requests;
        try {
            requests = toRequests(message.body());
        } catch (final Exception e) {
            // Unparseable now means unparseable on retry too; leave the
            // message so the redrive policy parks it in the DLQ for triage.
            log.error("Unparseable compile job message {}: {}", message.messageId(), message.body(), e);
            return;
        }

        try {
            for (final CompileTemplateRequest request : requests) {
                final List<CompileTemplateResponse> responses = templateCompileService.compile(request);
                for (final CompileTemplateResponse response : responses) {
                    log.info("Compiled {} / {} -> {}",
                            request.getPackageId(), response.getProfileId(), response.getArtifactLocation());
                }
            }
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(properties.compileQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .build());
        } catch (final Exception e) {
            log.error("Compile job failed ({}); message will be retried, then dead-lettered",
                    requests.stream().map(CompileTemplateRequest::getPackageId).toList(), e);
        }
    }

    /**
     * Maps a message body to compile requests. An empty list means "nothing
     * to do, acknowledge the message" (e.g. S3's TestEvent handshake, or an
     * S3 event for a non-trigger key).
     */
    private List<CompileTemplateRequest> toRequests(final String body) throws Exception {
        final JsonNode node = objectMapper.readTree(body);

        if (node.has("Records")) {
            return toS3EventRequests(node);
        }

        // S3 sends {"Event": "s3:TestEvent", ...} when the bucket
        // notification is configured; acknowledge and drop.
        if (node.path("Event").asText("").startsWith("s3:")) {
            return List.of();
        }

        final CompileTemplateRequest request = objectMapper.treeToValue(node, CompileTemplateRequest.class);
        if (request.getPackageId() == null || request.getPackageId().isBlank()) {
            throw new IllegalArgumentException("Message has no packageId: " + body);
        }
        return List.of(request);
    }

    private List<CompileTemplateRequest> toS3EventRequests(final JsonNode event) {
        final Set<String> packageIds = new LinkedHashSet<>();
        for (final JsonNode record : event.get("Records")) {
            if (!"aws:s3".equals(record.path("eventSource").asText())) {
                continue;
            }
            // Keys in S3 events are URL-encoded (space as '+').
            final String key = URLDecoder.decode(
                    record.path("s3").path("object").path("key").asText().replace("+", "%20"),
                    StandardCharsets.UTF_8);
            final Matcher matcher = PACKAGE_UPLOAD_KEY.matcher(key);
            if (matcher.matches()) {
                packageIds.add(matcher.group(1));
            }
        }

        final List<CompileTemplateRequest> requests = new ArrayList<>();
        for (final String packageId : packageIds) {
            final CompileTemplateRequest request = new CompileTemplateRequest();
            request.setPackageId(packageId);
            requests.add(request);
        }
        return requests;
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
