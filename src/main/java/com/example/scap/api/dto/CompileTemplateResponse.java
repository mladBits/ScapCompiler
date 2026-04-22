package com.example.scap.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CompileTemplateResponse {
    private String templateId;
    private String benchmarkId;
    private String profileId;
    private int selectedRuleCount;
    private int compiledCheckCount;
    private int definitionPlanCount;
    private int unsupportedTestCount;
    private Map<String, Long> compiledChecksByFamily = new HashMap<>();
}
