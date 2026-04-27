package com.example.scap.model.compiled;

import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.oval.CompiledOvalCheck;
import com.example.scap.variables.ResolvedVariableBindings;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class ExecutionTemplate {
    private String templateId;
    private String contentPackageId;
    private String contentVersion;
    private String benchmarkId;
    private String profileId;
    private String schemaVersion;
    private Instant generatedAt;

    private final List<CompiledTemplateRule> rules = new ArrayList<>();

    private ResolvedVariableBindings variableBindings;
    private LocalVariableCompilationResult localVariables;

    private final List<CompiledOvalCheck> checks = new ArrayList<>();

    private final List<String> unsupportedCheckTypes = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
}
