package com.example.scap.model.parsed.oval.variables;

public sealed interface ParsedOvalVariable
        permits ParsedOvalExternalVariable, ParsedOvalConstantVariable, ParsedOvalLocalVariable {
    String getId();
    String getDatatype();
}
