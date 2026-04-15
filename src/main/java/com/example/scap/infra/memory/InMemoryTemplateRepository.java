package com.example.scap.infra.memory;

import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;
import com.example.scap.port.TemplateRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTemplateRepository implements TemplateRepositoryPort {

    private final Map<TemplateKey, CompiledTemplate> store = new ConcurrentHashMap<>();

    @Override
    public void save(final CompiledTemplate template) {
        store.put(template.key(), template);
    }

    @Override
    public Optional<CompiledTemplate> findByKey(final TemplateKey key) {
        return Optional.ofNullable(store.get(key));
    }
}