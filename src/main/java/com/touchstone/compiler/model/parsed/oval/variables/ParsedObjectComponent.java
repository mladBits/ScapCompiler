package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedObjectComponent implements ParsedOvalVariableComponent {
    private String objectRef;
    private String itemField;
}

