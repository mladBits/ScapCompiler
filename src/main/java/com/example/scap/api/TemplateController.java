package com.example.scap.api;

import com.example.scap.api.dto.CompileTemplateRequest;
import com.example.scap.api.dto.CompileTemplateResponse;
import com.example.scap.service.TemplateCompileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateCompileService templateCompileService;
    //private final VariableCatalogService variableCatalogService;

    @PostMapping("/compile")
    public ResponseEntity<CompileTemplateResponse> compile(@Valid @RequestBody CompileTemplateRequest request) {
        return ResponseEntity.ok(templateCompileService.compile(request));
    }
}
