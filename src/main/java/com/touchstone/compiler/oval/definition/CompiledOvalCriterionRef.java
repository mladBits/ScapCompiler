package com.touchstone.compiler.oval.definition;

import lombok.Data;

@Data
public final class CompiledOvalCriterionRef implements CompiledOvalCriteriaNode {
    private String testId;
    private boolean negate;
    private boolean supported;
}
