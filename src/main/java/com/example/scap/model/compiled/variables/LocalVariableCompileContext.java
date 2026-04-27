package com.example.scap.model.compiled.variables;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;
import lombok.Data;

@Data
public class LocalVariableCompileContext {
    private final OvalIndex ovalIndex;
    private final VariableExpressionRecursiveCompiler recursiveCompiler;

    public CompiledVariableExpression compileChild(final ParsedOvalVariableComponent component) {
        return recursiveCompiler.compile(this, component);
    }
}
