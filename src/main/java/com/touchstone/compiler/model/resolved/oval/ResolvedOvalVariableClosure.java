package com.touchstone.compiler.model.resolved.oval;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ResolvedOvalVariableClosure {

    /**
     * Every variable id reachable from the slice (entity var_refs, filter
     * states, and nested variable components of local variables).
     */
    private final Set<String> variableIds = new LinkedHashSet<>();

    /**
     * Objects referenced by local variable object components. These need
     * collection plans even when no test references them.
     */
    private final Set<String> objectIds = new LinkedHashSet<>();

    /**
     * Direct object-component refs per variable, so assembly can mark a
     * variable unresolved when one of its objects fails to compile.
     */
    private final Map<String, Set<String>> objectIdsByVariableId = new LinkedHashMap<>();

    /**
     * Variables that cannot be evaluated (circular reference, missing
     * definition, unsupported function), keyed by variable id with a reason.
     */
    private final Map<String, String> unsupportedVariableReasons = new LinkedHashMap<>();
}