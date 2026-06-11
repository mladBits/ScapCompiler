package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;

@Data
public class ParsedOvalCriterion implements ParsedOvalCriteriaNode {
    private String testRef;
    private Boolean isNegated;
}
