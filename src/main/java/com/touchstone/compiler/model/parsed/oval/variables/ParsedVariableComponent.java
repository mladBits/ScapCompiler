package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedVariableComponent implements ParsedOvalVariableComponent {
    private String varRef;
}