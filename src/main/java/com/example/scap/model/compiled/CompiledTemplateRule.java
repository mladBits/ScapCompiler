package com.example.scap.model.compiled;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompiledTemplateRule {
    private String ruleId;
    private String title;
    private final List<String> ovalDefinitionIds = new ArrayList<>();
}
