package com.touchstone.compiler.model.compiled.variables.function;

import com.touchstone.compiler.model.compiled.variables.CompiledVariableExpression;
import com.touchstone.compiler.model.compiled.variables.CompiledVariableRefExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompileContext;
import com.touchstone.compiler.model.compiled.variables.VariableFunctionCompiler;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedVariableComponent;
import org.springframework.stereotype.Component;

@Component
public class VariableComponentCompiler implements VariableFunctionCompiler<ParsedVariableComponent> {

    @Override
    public boolean supports(final ParsedOvalVariableComponent component) {
        return component instanceof ParsedVariableComponent;
    }

    @Override
    public CompiledVariableExpression compile(
            final LocalVariableCompileContext context,
            final ParsedVariableComponent component
    ) {
        return new CompiledVariableRefExpression(component.getVarRef());
    }
}