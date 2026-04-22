package com.example.scap.oval.definition;

public sealed interface CompiledOvalCriteriaNode
    permits CompiledOvalCriteriaGroup,
            CompiledOvalCriterionRef,
        CompiledOvalExtendDefinitionRef {
}
