package com.example.scap.model.parsed.oval;

import com.example.scap.model.parsed.oval.variables.ParsedOvalVariable;
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
