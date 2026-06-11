package com.touchstone.compiler.model.compiled.variables.function;

import com.touchstone.compiler.model.compiled.variables.CompiledConcatExpression;
import com.touchstone.compiler.model.compiled.variables.CompiledVariableExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompileContext;
import com.touchstone.compiler.model.compiled.variables.VariableFunctionCompiler;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedConcatComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
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
