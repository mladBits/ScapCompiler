package com.example.scap.service;

import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableDefinition;
import com.example.scap.port.TemplateRepositoryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VariableCatalogService {
    private final TemplateRepositoryPort templateRepositoryPort;

    public List<VariableDefinition> getCatalog(final TemplateKey key) {
        return templateRepositoryPort.findByKey(key)
                .map(CompiledTemplate::variableCatalog)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }
}