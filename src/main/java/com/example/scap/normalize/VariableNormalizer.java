package com.example.scap.normalize;

import com.example.scap.model.VariableDefinition;
import com.example.scap.parser.OvalParser;
import com.example.scap.parser.XccdfParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VariableNormalizer {

    public List<VariableDefinition> normalize(
            XccdfParser.ParsedXccdf xccdf,
            OvalParser.ParsedOval oval
    ) {
        return xccdf.values().stream()
                .map(v -> new VariableDefinition(
                        v.id(),
                        v.title(),
                        v.type(),
                        false,
                        false,
                        v.defaultValue(),
                        List.of(),
                        null,
                        null
                ))
                .toList();
    }
}
