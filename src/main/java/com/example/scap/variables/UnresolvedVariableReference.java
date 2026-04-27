package com.example.scap.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class UnresolvedVariableReference implements VariableAwareEntityValue {
    private String variableId;
}
