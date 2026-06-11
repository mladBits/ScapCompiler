package com.touchstone.compiler.variables;

import com.touchstone.compiler.model.compiled.variables.CompiledLocalVariableExpression;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.normalized.oval.LiteralValueExpression;
import com.touchstone.compiler.model.normalized.oval.OvalEntityConstraint;
import com.touchstone.compiler.model.normalized.oval.OvalValueExpression;
import com.touchstone.compiler.model.normalized.oval.VariableValueExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class VariableAwareEntityResolverImpl implements VariableAwareEntityResolver  {
    @Override
    public VariableAwareEntityValue resolve(
            final OvalEntityConstraint constraint,
            final ResolvedVariableBindings bindings,
            final LocalVariableCompilationResult localVariables) {
        if (constraint == null || constraint.getValue() == null) {
            return null;
        }

        final OvalValueExpression expression = constraint.getValue();

        if (expression instanceof LiteralValueExpression literal) {
            return new LiteralEntityValue(literal.getValue());
        }

        if (expression instanceof VariableValueExpression variableExpression) {
            final String variableId = variableExpression.getVariableId();

            final VariableBinding binding = bindings.getBindingsById().get(variableId);
            if (binding != null) {
                return new BoundVariableReference(
                        variableId,
                        new ArrayList<>(binding.getValues()),
                        binding.getSource()
                );
            }

            final CompiledLocalVariableExpression localVariable =
                    localVariables.getLocalVariablesById().get(variableId);

            if (localVariable != null) {
                return new RuntimeVariableReference(variableId, localVariable);
            }

            return new UnresolvedVariableReference(variableId);
        }

        throw new IllegalArgumentException(
                "Unsupported OvalValueExpression type: " + expression.getClass().getName()
        );
    }
}
