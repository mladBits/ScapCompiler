package com.example.scap.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class ParsedOvalLocalVariable implements ParsedOvalVariable {
    private String id;
    private String datatype;
    private ParsedOvalVariableComponent expression;
}
