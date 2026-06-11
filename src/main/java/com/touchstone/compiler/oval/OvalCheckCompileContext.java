package com.touchstone.compiler.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.compiled.CompiledState;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.variables.ResolvedVariableBindings;
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
    private final Map<String, CompiledState> states;

}
