package com.example.scap.model.compiled.variables.function;

import com.example.scap.model.compiled.variables.CompiledVariableExpression;
import com.example.scap.model.compiled.variables.CompiledVariableRefExpression;
import com.example.scap.model.compiled.variables.LocalVariableCompileContext;
import com.example.scap.model.compiled.variables.VariableFunctionCompiler;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.example.scap.model.parsed.oval.variables.ParsedVariableComponent;
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