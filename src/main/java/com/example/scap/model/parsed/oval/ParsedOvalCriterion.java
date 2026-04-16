package com.example.scap.model.parsed.oval;

import lombok.Data;

@Data
public class ParsedOvalCriterion implements ParsedOvalCriteriaNode {
    private String testRef;
    private Boolean isNegated;
}
