package com.touchstone.compiler.model.compiled.variables.function;

import com.touchstone.compiler.model.compiled.variables.CompiledRegexCaptureExpression;
import com.touchstone.compiler.model.compiled.variables.CompiledVariableExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompileContext;
import com.touchstone.compiler.model.compiled.variables.VariableFunctionCompiler;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedRegexCaptureComponent;
import org.springframework.stereotype.Component;

@Component
public class RegexCaptureComponentCompiler implements VariableFunctionCompiler<ParsedRegexCaptureComponent> {

    @Override
    public boolean supports(final ParsedOvalVariableComponent component) {
        return component instanceof ParsedRegexCaptureComponent;
    }

    @Override
    public CompiledVariableExpression compile(
            final LocalVariableCompileContext context,
            final ParsedRegexCaptureComponent component
    ) {
        return new CompiledRegexCaptureExpression(
                component.getPattern(),
                context.compileChild(component.getComponent())
        );
    }
}
