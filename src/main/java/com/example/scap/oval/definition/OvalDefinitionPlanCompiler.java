package com.example.scap.oval.definition;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.oval.OvalCheckCompilationResult;

import java.util.Collection;
import java.util.List;

public interface OvalDefinitionPlanCompiler {
    List<CompiledOvalDefinitionPlan> compile(
            Collection<ParsedOvalDefinition> definitions,
            OvalCheckCompilationResult checkCompilationResult
    );
}
