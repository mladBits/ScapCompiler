package com.example.scap.service;

import com.example.scap.compile.TemplateCompiler;
import com.example.scap.model.CompiledTemplate;
import com.example.scap.model.TemplateKey;
import com.example.scap.model.VariableDefinition;
import com.example.scap.normalize.VariableNormalizer;
import com.example.scap.parser.OvalParser;
import com.example.scap.parser.XccdfParser;
import com.example.scap.port.TemplateRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateCompileService {
    private final XccdfParser xccdfParser;
    private final OvalParser ovalParser;
    private final VariableNormalizer variableNormalizer;
    private final TemplateCompiler templateCompiler;
    private final TemplateRepositoryPort templateRepositoryPort;

    public CompiledTemplate compile(
            final TemplateKey key,
            final byte[] xccdfBytes,
            final byte[] ovalBytes
    ) {
        final XccdfParser.ParsedXccdf xccdf = xccdfParser.parse(xccdfBytes);
        final OvalParser.ParsedOval oval = ovalParser.parse(ovalBytes);
        final List<VariableDefinition> variables = variableNormalizer.normalize(xccdf, oval);

        final CompiledTemplate template = templateCompiler.compile(key, variables);
        templateRepositoryPort.save(template);
        return template;
    }
}
