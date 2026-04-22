package com.example.scap.normalize;

import com.example.scap.model.normalized.oval.LiteralValueExpression;
import com.example.scap.model.normalized.oval.OvalEntityConstraint;
import com.example.scap.model.normalized.oval.VariableValueExpression;
import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import org.springframework.stereotype.Component;

@Component
public class OvalEntityNormalizer {
    public OvalEntityConstraint normalize(final ParsedOvalEntity entity) {
        final OvalEntityConstraint constraint = new OvalEntityConstraint();

        constraint.setEntityName(entity.getName());
        constraint.setDatatype(entity.getAttributes().get("datatype"));
        constraint.setOperation(defaultIfBlank(entity.getAttributes().get("operation"), "equals"));

        final String varRef = entity.getAttributes().get("var_ref");

        if (isNotBlank(varRef)) {
            constraint.setValue(new VariableValueExpression(varRef.trim()));
        } else {
            constraint.setValue(new LiteralValueExpression(entity.getValue()));
        }

        return constraint;
    }

    private String defaultIfBlank(final String value, final String defaultValue) {
        return isNotBlank(value) ? value.trim() : defaultValue;
    }

    private boolean isNotBlank(final String value) {
        return value != null && !value.isBlank();
    }
}
