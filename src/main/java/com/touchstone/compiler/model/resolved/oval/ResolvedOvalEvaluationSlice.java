package com.touchstone.compiler.model.resolved.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalDefinition;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObjectBase;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
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
