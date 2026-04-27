package com.example.scap.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public final class CompiledConcatExpression implements CompiledVariableExpression {
    private List<CompiledVariableExpression> components;
}

