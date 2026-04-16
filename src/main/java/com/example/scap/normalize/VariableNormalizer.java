package com.example.scap.normalize;

import com.example.scap.model.VariableDefinition;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VariableNormalizer {

    public List<VariableDefinition> normalize(
            ParsedXccdfBenchmark benchmark,
            ParsedXccdfProfile profile
    ) {
        throw new UnsupportedOperationException("");
    }
}
