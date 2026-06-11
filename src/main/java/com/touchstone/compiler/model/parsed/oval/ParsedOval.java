package com.touchstone.compiler.model.parsed.oval;

import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariable;
import lombok.Data;

import java.util.List;

@Data
public class ParsedOval {
    private List<ParsedOvalDefinition> definitions;
    private List<ParsedOvalTest> tests;
    private List<ParsedOvalObjectBase> objects;
    private List<ParsedOvalState> states;
    private List<ParsedOvalVariable> variables;
}
