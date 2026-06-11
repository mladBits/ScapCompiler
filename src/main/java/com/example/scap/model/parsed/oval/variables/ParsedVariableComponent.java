package com.example.scap.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedVariableComponent implements ParsedOvalVariableComponent {
    private String varRef;
}