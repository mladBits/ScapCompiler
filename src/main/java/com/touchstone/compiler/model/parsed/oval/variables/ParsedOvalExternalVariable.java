package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

@Data
public final class  ParsedOvalExternalVariable implements ParsedOvalVariable {
    private String id;
    private String datatype;
}
