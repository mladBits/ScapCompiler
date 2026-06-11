package com.touchstone.compiler.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public final class BoundVariableReference implements VariableAwareEntityValue {
    private String variableId;
    private List<String> values;
    private VariableBindingSource source;
}
