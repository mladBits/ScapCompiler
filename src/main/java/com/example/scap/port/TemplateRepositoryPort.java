package com.example.scap.port;

import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;

import java.util.Optional;

public interface TemplateRepositoryPort {
    void save(CompiledTemplate template);
    Optional<CompiledTemplate> findByKey(TemplateKey key);
}
