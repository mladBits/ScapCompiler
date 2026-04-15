package com.example.scap.compile;

import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableDefinition;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateCompiler {

    public CompiledTemplate compile(
            final TemplateKey key,
            final List<VariableDefinition> variables
    ) {
        final Map<String, Object> executionPlan = new HashMap<>();
        executionPlan.put("status", "PLACEHOLDER");
        executionPlan.put("collectors", List.of());
        executionPlan.put("definitions", List.of());

        return new CompiledTemplate(
                key,
                variables,
                executionPlan,
                Instant.now()
        );
    }
}
