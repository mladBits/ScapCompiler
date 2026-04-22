package com.example.scap.oval.definition;

import lombok.Data;

@Data
public final class CompiledOvalExtendDefinitionRef implements CompiledOvalCriteriaNode {
    private String definitionId;
    private boolean negate;
}
