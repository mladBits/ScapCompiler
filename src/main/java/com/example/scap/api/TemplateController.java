package com.example.scap.api;

import com.example.scap.api.dto.CompileTemplateRequest;
import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableDefinition;
import com.example.scap.port.ContentStoragePort;
import com.example.scap.service.TemplateCompileService;
import com.example.scap.service.VariableCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    private final ContentStoragePort contentStoragePort;
    private final TemplateCompileService templateCompileService;
    private final VariableCatalogService variableCatalogService;

    @PostMapping("/compile")
    public CompiledTemplate compile(@Valid @RequestBody CompileTemplateRequest request) {
        TemplateKey key = new TemplateKey(
                request.benchmarkId(),
                request.profileId(),
                request.contentVersion()
        );

        byte[] xccdfBytes = contentStoragePort.get(request.xccdfStorageKey());
        byte[] ovalBytes = contentStoragePort.get(request.ovalStorageKey());

        return templateCompileService.compile(key, xccdfBytes, ovalBytes);
    }

    @GetMapping("/{benchmarkId}/{profileId}/{contentVersion}/variables")
    public List<VariableDefinition> variables(
            @PathVariable String benchmarkId,
            @PathVariable String profileId,
            @PathVariable String contentVersion
    ) {
        return variableCatalogService.getCatalog(new TemplateKey(benchmarkId, profileId, contentVersion));
    }
}
