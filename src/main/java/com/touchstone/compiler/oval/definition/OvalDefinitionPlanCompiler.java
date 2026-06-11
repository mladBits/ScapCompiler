package com.touchstone.compiler.oval.definition;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalDefinition;
import com.touchstone.compiler.oval.OvalCheckCompilationResult;

import java.util.Collection;
import java.util.List;

public interface OvalDefinitionPlanCompiler {
    List<CompiledOvalDefinitionPlan> compile(
            Collection<ParsedOvalDefinition> definitions,
            OvalCheckCompilationResult checkCompilationResult
    );
}
