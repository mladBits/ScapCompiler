package com.touchstone.compiler.model.compiled.variables.function;

import com.touchstone.compiler.model.compiled.variables.CompiledObjectComponentExpression;
import com.touchstone.compiler.model.compiled.variables.CompiledVariableExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompileContext;
import com.touchstone.compiler.model.compiled.variables.VariableFunctionCompiler;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedObjectComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import org.springframework.stereotype.Component;

@Component
public class ObjectComponentCompiler implements VariableFunctionCompiler<ParsedObjectComponent> {

    @Override
    public boolean supports(final ParsedOvalVariableComponent component) {
        return component instanceof ParsedObjectComponent;
    }

    @Override
    public CompiledVariableExpression compile(
            final LocalVariableCompileContext context,
            final ParsedObjectComponent component
    ) {
        return new CompiledObjectComponentExpression(
                component.getObjectRef(),
                component.getItemField()
        );
    }
}
