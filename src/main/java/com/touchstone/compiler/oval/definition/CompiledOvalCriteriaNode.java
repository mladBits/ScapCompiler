package com.touchstone.compiler.oval.definition;

public sealed interface CompiledOvalCriteriaNode
    permits CompiledOvalCriteriaGroup,
            CompiledOvalCriterionRef,
        CompiledOvalExtendDefinitionRef {
}
