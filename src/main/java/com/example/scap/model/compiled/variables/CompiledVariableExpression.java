package com.example.scap.model.compiled.variables;

public sealed interface CompiledVariableExpression
        permits CompiledConcatExpression, CompiledLiteralExpression, CompiledObjectComponentExpression, CompiledRegexCaptureExpression {
}
