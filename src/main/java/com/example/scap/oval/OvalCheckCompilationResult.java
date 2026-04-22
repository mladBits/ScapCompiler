package com.example.scap.oval;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OvalCheckCompilationResult {
    private final List<CompiledOvalCheck> compiledChecks = new ArrayList<>();
    private final List<String> unsupportedTestIds = new ArrayList<>();
}
