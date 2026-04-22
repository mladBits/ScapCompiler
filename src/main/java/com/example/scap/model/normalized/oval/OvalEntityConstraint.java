package com.example.scap.model.normalized.oval;

import lombok.Data;

@Data
public class OvalEntityConstraint {
    private String entityName;
    private String datatype;
    private String operation;
    private OvalValueExpression value;
}
