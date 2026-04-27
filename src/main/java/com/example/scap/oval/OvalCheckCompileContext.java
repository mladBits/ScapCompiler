package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.variables.ResolvedVariableBindings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class OvalCheckCompileContext {
    private final OvalIndex ovalIndex;
    private final ResolvedOvalEvaluationSlice slice;
    private final ResolvedVariableBindings variableBindings;
    private final LocalVariableCompilationResult localVariables;
    private final Map<String, CompiledObjectPlan> objects;

}
