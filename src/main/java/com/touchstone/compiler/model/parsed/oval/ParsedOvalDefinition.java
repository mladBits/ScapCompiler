package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;

@Data
public class ParsedOvalDefinition {
    private String id;
    private String defClass;
    private ParsedOvalCriteriaNode criteria;
}
