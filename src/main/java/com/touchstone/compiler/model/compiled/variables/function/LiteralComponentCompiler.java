package com.touchstone.compiler.model.compiled.variables.function;

import com.touchstone.compiler.model.compiled.variables.CompiledLiteralExpression;
import com.touchstone.compiler.model.compiled.variables.CompiledVariableExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompileContext;
import com.touchstone.compiler.model.compiled.variables.VariableFunctionCompiler;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedLiteralComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import org.springframework.stereotype.Component;


@Component
public class LiteralComponentCompiler implements VariableFunctionCompiler<ParsedLiteralComponent> {

    @Override
    public boolean supports(final ParsedOvalVariableComponent component) {
        return component instanceof ParsedLiteralComponent;
    }

    @Override
    public CompiledVariableExpression compile(
            final LocalVariableCompileContext context,
            final ParsedLiteralComponent component
    ) {
        return new CompiledLiteralExpression(component.getValue());
    }
}
