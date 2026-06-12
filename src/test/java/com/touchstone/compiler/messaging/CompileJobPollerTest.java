package com.touchstone.compiler.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.api.dto.CompileTemplateResponse;
import com.touchstone.compiler.config.AwsProperties;
import com.touchstone.compiler.service.TemplateCompileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CompileJobPollerTest {

    private static final String QUEUE_URL = "http://localhost:4566/000000000000/touchstone-compile-jobs";

    private SqsClient sqsClient;
    private TemplateCompileService templateCompileService;
    private CompileJobPoller poller;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        templateCompileService = mock(TemplateCompileService.class);
        AwsProperties properties = new AwsProperties(
                "us-east-1", "http://localhost:4566", "test", "test",
                "touchstone-raw-content", "touchstone-compiled-templates", QUEUE_URL);
        poller = new CompileJobPoller(sqsClient, properties, templateCompileService, new ObjectMapper());
    }

    @Test
    void handle_shouldCompileAndDeleteOnSuccess() throws Exception {
        when(templateCompileService.compile(any())).thenReturn(List.of(new CompileTemplateResponse()));
        Message message = Message.builder()
                .body("{\"packageId\":\"CIS_Test_Benchmark_v1.0.0\",\"profileIds\":[\"profile-1\",\"profile-2\"]}")
                .receiptHandle("rh-1")
                .build();

        poller.handle(message);

        ArgumentCaptor<CompileTemplateRequest> request = ArgumentCaptor.forClass(CompileTemplateRequest.class);
        verify(templateCompileService).compile(request.capture());
        assertEquals("CIS_Test_Benchmark_v1.0.0", request.getValue().getPackageId());
        assertEquals(List.of("profile-1", "profile-2"), request.getValue().getProfileIds());

        ArgumentCaptor<DeleteMessageRequest> delete = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(sqsClient).deleteMessage(delete.capture());
        assertEquals("rh-1", delete.getValue().receiptHandle());
        assertEquals(QUEUE_URL, delete.getValue().queueUrl());
    }

    @Test
    void handle_shouldNotDeleteWhenCompileFails() throws Exception {
        when(templateCompileService.compile(any())).thenThrow(new IllegalStateException("boom"));
        Message message = Message.builder()
                .body("{\"packageId\":\"b\",\"profileIds\":[\"p\"]}")
                .receiptHandle("rh-2")
                .build();

        poller.handle(message);

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void handle_shouldNotDeleteOrCompileUnparseableMessage() {
        Message message = Message.builder()
                .body("not json")
                .receiptHandle("rh-3")
                .build();

        poller.handle(message);

        verifyNoInteractions(templateCompileService);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
