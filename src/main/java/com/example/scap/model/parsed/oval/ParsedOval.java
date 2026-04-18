package com.example.scap.model.parsed.oval;

import lombok.Data;

import java.util.List;

@Data
public class ParsedOval {
    private List<ParsedOvalDefinition> definitions;
    private List<ParsedOvalTest> tests;
}
