package com.example.scap.model.parsed.oval;

import lombok.Data;

@Data
public class ParsedOvalDefinition {
    private String id;
    private String defClass;
    private ParsedOvalCriteriaNode criteria;
}
