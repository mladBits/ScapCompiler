package com.example.scap.model.resolved.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.model.parsed.oval.ParsedOvalObjectBase;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResolvedOvalEvaluationSlice {
    private List<ParsedOvalDefinition> definitions;
    private List<ParsedOvalTest> tests;
    private List<ParsedOvalObjectBase> objects;
    private List<ParsedOvalState> states;

    public ResolvedOvalEvaluationSlice(
            final List<ParsedOvalDefinition> definitions,
            final List<ParsedOvalTest> tests) {

        this(definitions, tests, List.of(), List.of());
    }
}
