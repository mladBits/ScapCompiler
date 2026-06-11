package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedLiteralComponent implements ParsedOvalVariableComponent {
    private String value;
}
