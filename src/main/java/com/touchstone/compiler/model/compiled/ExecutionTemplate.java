package com.touchstone.compiler.model.compiled;

import com.touchstone.compiler.model.compiled.variables.CompiledVariable;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.CompiledOvalCheck;
import com.touchstone.compiler.oval.definition.CompiledOvalDefinitionPlan;
import com.touchstone.compiler.variables.ResolvedVariableBindings;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExecutionTemplate {
    private String templateId;
    private String contentPackageId;
    private String contentVersion;
    private String benchmarkId;
    private String profileId;
    private String schemaVersion;
    private Instant generatedAt;

    /**
     * Selected XCCDF rules included in this template.
     */
    private final List<CompiledTemplateRule> rules = new ArrayList<>();

    /**
     * Compiled variables keyed by variable id.
     * The agent resolves these lazily and caches results.
     */
    private final Map<String, CompiledVariable> variablesById = new LinkedHashMap<>();

    /**
     * Compiled objects keyed by object id.
     * The agent executes these recursively by object id.
     */
    private final Map<String, CompiledObjectPlan> objectsById = new LinkedHashMap<>();

    /**
     * Compiled states keyed by state id.
     * Checks and filter tasks reference these by id.
     */
    private final Map<String, CompiledState> statesById = new LinkedHashMap<>();

    /**
     * Compiled OVAL checks for this profile/rule selection.
     * Each check references a root object id and state ids.
     */
    private final List<CompiledOvalCheck> checks = new ArrayList<>();

    /**
     * Compiled definition plans used to produce final OVAL definition results.
     */
    private final List<CompiledOvalDefinitionPlan> definitionPlans = new ArrayList<>();

    /**
     * Optional diagnostics for things intentionally excluded from execution.
     */
    private final List<String> unsupportedCheckTypes = new ArrayList<>();
    private final List<String> unsupportedTestIds = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
}
