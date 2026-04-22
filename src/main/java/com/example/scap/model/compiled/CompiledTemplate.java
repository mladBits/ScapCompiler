package com.example.scap.model.compiled;

import com.example.scap.oval.CompiledOvalCheck;
import com.example.scap.oval.definition.CompiledOvalDefinitionPlan;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompiledTemplate {
    private String templateId;
    private String contentPackageId;
    private String contentVersion;
    private String benchmarkId;
    private String profileId;
    private Instant generatedAt;

    private final List<CompiledTemplateRule> rules = new ArrayList<>();
    private final List<CompiledOvalCheck> compiledChecks = new ArrayList<>();
    private final List<CompiledOvalDefinitionPlan> definitionPlans = new ArrayList<>();
    private final List<String> unsupportedTestIds = new ArrayList<>();
}
