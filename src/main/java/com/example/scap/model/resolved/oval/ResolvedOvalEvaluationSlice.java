package com.example.scap.model.resolved.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResolvedOvalEvaluationSlice {
    private List<ParsedOvalDefinition> definitions;
    private List<ParsedOvalTest> tests;
}
