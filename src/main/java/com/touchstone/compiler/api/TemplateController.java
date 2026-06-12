package com.touchstone.compiler.api;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.api.dto.CompileTemplateResponse;
import com.touchstone.compiler.service.TemplateCompileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Synchronous compile endpoint for debugging and content-coverage checks
 * (the response surfaces unsupportedCheckTypes/warnings immediately). The
 * SQS poller is the production trigger; disable this endpoint outside dev.
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.compile-endpoint.enabled", havingValue = "true")
public class TemplateController {
    private final TemplateCompileService templateCompileService;
    //private final VariableCatalogService variableCatalogService;

    @PostMapping("/compile")
    public ResponseEntity<List<CompileTemplateResponse>> compile(@Valid @RequestBody CompileTemplateRequest request) throws Exception {
        return ResponseEntity.ok(templateCompileService.compile(request));
    }
}
