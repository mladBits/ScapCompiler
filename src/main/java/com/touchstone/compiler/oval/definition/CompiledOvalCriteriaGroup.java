package com.touchstone.compiler.oval.definition;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public final class CompiledOvalCriteriaGroup implements CompiledOvalCriteriaNode {
    private String operator;
    private boolean negate;
    private final List<CompiledOvalCriteriaNode> children = new ArrayList<>();
}
