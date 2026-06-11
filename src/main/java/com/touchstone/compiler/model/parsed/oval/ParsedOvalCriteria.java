package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOvalCriteria implements ParsedOvalCriteriaNode {
    private String operator;
    private Boolean isNegated;
    private List<ParsedOvalCriteriaNode> children = new ArrayList<>();
}
