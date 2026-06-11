package com.touchstone.compiler.api;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.api.dto.CompileTemplateResponse;
import com.touchstone.compiler.service.TemplateCompileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateCompileService templateCompileService;
    //private final VariableCatalogService variableCatalogService;

    @PostMapping("/compile")
    public ResponseEntity<CompileTemplateResponse> compile(@Valid @RequestBody CompileTemplateRequest request) throws Exception {
        return ResponseEntity.ok(templateCompileService.compile(request));
    }
}
