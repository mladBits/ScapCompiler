package com.example.scap.oval;

import lombok.Getter;

import java.util.*;

@Getter
public class OvalCheckCompilationResult {
    private final List<CompiledOvalCheck> compiledChecks = new ArrayList<>();
    private final Map<String, CompiledObjectPlan> objects = new HashMap<>();
    private final Set<String> unsupportedCheckTypes = new HashSet<>();
}
