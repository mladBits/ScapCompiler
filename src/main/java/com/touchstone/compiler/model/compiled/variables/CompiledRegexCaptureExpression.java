package com.touchstone.compiler.model.compiled.variables;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public final class CompiledRegexCaptureExpression implements CompiledVariableExpression {
    private String pattern;
    private CompiledVariableExpression component;
}
