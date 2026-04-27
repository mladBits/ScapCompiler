package com.example.scap.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedRegexCaptureComponent implements ParsedOvalVariableComponent {
    private String pattern;
    private ParsedOvalVariableComponent component;
}

