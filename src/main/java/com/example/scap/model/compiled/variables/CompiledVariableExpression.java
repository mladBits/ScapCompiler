package com.example.scap.model.compiled.variables;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "function")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CompiledLiteralExpression.class, name = "literal"),
        @JsonSubTypes.Type(value = CompiledConcatExpression.class, name = "concat"),
        @JsonSubTypes.Type(value = CompiledObjectComponentExpression.class, name = "object"),
        @JsonSubTypes.Type(value = CompiledRegexCaptureExpression.class, name = "regex_capture"),
        @JsonSubTypes.Type(value = CompiledVariableRefExpression.class, name = "variable")
})
public sealed interface CompiledVariableExpression
        permits CompiledConcatExpression,
        CompiledLiteralExpression,
        CompiledObjectComponentExpression,
        CompiledRegexCaptureExpression,
        CompiledVariableRefExpression {
}