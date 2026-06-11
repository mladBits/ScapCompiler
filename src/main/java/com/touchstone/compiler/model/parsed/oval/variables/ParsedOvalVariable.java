package com.touchstone.compiler.model.parsed.oval.variables;

public sealed interface ParsedOvalVariable
        permits ParsedOvalExternalVariable, ParsedOvalConstantVariable, ParsedOvalLocalVariable {
    String getId();
    String getDatatype();
}
