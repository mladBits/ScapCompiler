package com.touchstone.compiler.oval;

import com.touchstone.compiler.model.compiled.CompiledState;
import lombok.Getter;

import java.util.*;

@Getter
public class OvalCheckCompilationResult {
    private final List<CompiledOvalCheck> compiledChecks = new ArrayList<>();
    private final Map<String, CompiledObjectPlan> objects = new HashMap<>();
    private final Map<String, CompiledState> states = new HashMap<>();
    private final Set<String> unsupportedCheckTypes = new HashSet<>();

    /**
     * Variable-referenced objects that could not be compiled (no compiler for
     * the object type, missing object, compile failure). Variables depending
     * on these are emitted as UNRESOLVED.
     */
    private final Set<String> failedObjectIds = new HashSet<>();
    private final List<String> warnings = new ArrayList<>();
}