package com.touchstone.compiler.model.parsed.oval.variables;

public sealed interface ParsedOvalVariableComponent
        permits ParsedLiteralComponent,
        ParsedObjectComponent,
        ParsedConcatComponent,
        ParsedRegexCaptureComponent,
        ParsedVariableComponent,
        ParsedUnsupportedComponent {
}