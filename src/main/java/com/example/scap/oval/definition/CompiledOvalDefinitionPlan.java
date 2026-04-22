package com.example.scap.oval.definition;

import lombok.Data;

@Data
public class CompiledOvalDefinitionPlan {
    private String id;
    private String defClass;
    private String title;
    private CompiledOvalCriteriaNode criteria;
}
