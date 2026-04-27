package com.example.scap.model.parsed.oval.variables;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class ParsedOvalConstantVariable implements ParsedOvalVariable {
    private String id;
    private String datatype;
    private List<String> values = new ArrayList<>();
}
