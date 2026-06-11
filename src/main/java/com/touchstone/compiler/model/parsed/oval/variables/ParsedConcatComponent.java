package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class ParsedConcatComponent implements ParsedOvalVariableComponent {
    private List<ParsedOvalVariableComponent> components = new ArrayList<>();
}
