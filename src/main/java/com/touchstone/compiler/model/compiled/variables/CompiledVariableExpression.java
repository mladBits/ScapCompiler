package com.touchstone.compiler.model.compiled.variables;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "node")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompiledLiteralExpression.class, name = "literal_component"),
        @JsonSubTypes.Type(value = CompiledConcatExpression.class, name = "concat"),
        @JsonSubTypes.Type(value = CompiledObjectComponentExpression.class, name = "object_component"),
        @JsonSubTypes.Type(value = CompiledRegexCaptureExpression.class, name = "regex_capture"),
        @JsonSubTypes.Type(value = CompiledVariableRefExpression.class, name = "variable_component")
})
public sealed interface CompiledVariableExpression
        permits CompiledConcatExpression,
        CompiledLiteralExpression,
        CompiledObjectComponentExpression,
        CompiledRegexCaptureExpression,
        CompiledVariableRefExpression {
}