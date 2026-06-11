package com.touchstone.compiler.model.compiled.variables;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import lombok.Data;

@Data
public class LocalVariableCompileContext {
    private final OvalIndex ovalIndex;
    private final VariableExpressionRecursiveCompiler recursiveCompiler;

    public CompiledVariableExpression compileChild(final ParsedOvalVariableComponent component) {
        return recursiveCompiler.compile(this, component);
    }
}
