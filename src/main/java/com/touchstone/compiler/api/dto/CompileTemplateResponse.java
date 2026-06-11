package com.touchstone.compiler.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CompileTemplateResponse {
    private String templateId;
    private String benchmarkId;
    private String profileId;

    private String contentPackageId;
    private String contentVersion;

    private String artifactLocation;
    private String artifactFormat;
    private String artifactVersion;

    private int selectedRuleCount;
    private int compiledCheckCount;
    private int definitionPlanCount;
    private int unsupportedTestCount;
    private int collectionTaskCount;

    private Map<String, Long> compiledChecksByFamily = new HashMap<>();
    private List<String> unsupportedTestsByFamily = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();
}
