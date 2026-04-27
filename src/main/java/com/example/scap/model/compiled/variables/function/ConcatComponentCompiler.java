package com.example.scap.model.compiled.variables.function;

import com.example.scap.model.compiled.variables.CompiledConcatExpression;
import com.example.scap.model.compiled.variables.CompiledVariableExpression;
import com.example.scap.model.compiled.variables.LocalVariableCompileContext;
import com.example.scap.model.compiled.variables.VariableFunctionCompiler;
import com.example.scap.model.parsed.oval.variables.ParsedConcatComponent;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;
import org.springframework.stereotype.Component;

@Component
public class ConcatComponentCompiler implements VariableFunctionCompiler<ParsedConcatComponent> {

    @Override
    public boolean supports(final ParsedOvalVariableComponent component) {
        return component instanceof ParsedConcatComponent;
    }

    @Override
    public CompiledVariableExpression compile(
            final LocalVariableCompileContext context,
            final ParsedConcatComponent component
    ) {
        return new CompiledConcatExpression(
                component.getComponents().stream()
                        .map(context::compileChild)
                        .toList()
        );
    }
}
