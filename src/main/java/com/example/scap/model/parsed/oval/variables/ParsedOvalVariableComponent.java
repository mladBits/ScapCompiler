package com.example.scap.model.parsed.oval.variables;

public sealed interface ParsedOvalVariableComponent
        permits ParsedLiteralComponent,
        ParsedObjectComponent,
        ParsedConcatComponent,
        ParsedRegexCaptureComponent {
}