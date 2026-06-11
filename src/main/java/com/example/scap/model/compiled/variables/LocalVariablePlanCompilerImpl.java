package com.example.scap.model.compiled.variables;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.variables.ParsedOvalLocalVariable;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariable;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.example.scap.model.parsed.oval.variables.ParsedUnsupportedComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalVariablePlanCompilerImpl implements LocalVariablePlanCompiler {
    private final List<VariableFunctionCompiler<?>> functionCompilers;

    @Override
    public LocalVariableCompilationResult compile(
            final OvalIndex ovalIndex,
            final Collection<String> variableIds) {
        final LocalVariableCompilationResult result = new LocalVariableCompilationResult();
        final LocalVariableCompileContext context =
                new LocalVariableCompileContext(ovalIndex, this::compileComponent);


        for (final String variableId : variableIds) {
            final ParsedOvalVariable variable = ovalIndex.getVariableById().get(variableId);

            if (!(variable instanceof ParsedOvalLocalVariable localVariable)) {
                continue;
            }

            try {
                final CompiledLocalVariableExpression compiled = new CompiledLocalVariableExpression();
                compiled.setVariableId(localVariable.getId());
                compiled.setDatatype(localVariable.getDatatype());
                compiled.setExpression(compileComponent(context, localVariable.getExpression()));

                result.getLocalVariablesById().put(compiled.getVariableId(), compiled);
            } catch (final Exception e) {
                log.warn("Failed to compile local variable {}: {}", variableId, e.getMessage());
                result.getUnsupportedVariableReasons().put(variableId, e.getMessage());
            }
        }

        return result;
    }

    private CompiledVariableExpression compileComponent(
            final LocalVariableCompileContext context,
            final ParsedOvalVariableComponent component) {
        if (component instanceof ParsedUnsupportedComponent unsupported) {
            throw new IllegalArgumentException(
                    "unsupported variable function '" + unsupported.getFunctionName() + "'");
        }

        for (final VariableFunctionCompiler<?> compiler : functionCompilers) {
            if (compiler.supports(component)) {
                @SuppressWarnings("unchecked")
                final VariableFunctionCompiler<ParsedOvalVariableComponent> typed =
                        (VariableFunctionCompiler<ParsedOvalVariableComponent>) compiler;
                return typed.compile(context, component);
            }
        }

        throw new IllegalArgumentException(
                "Unsupported local variable component: " + component.getClass().getName()
        );
    }
}
