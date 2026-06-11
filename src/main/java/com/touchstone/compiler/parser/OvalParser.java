package com.touchstone.compiler.parser;

import com.touchstone.compiler.model.parsed.oval.ParsedOval;

import java.io.InputStream;

public interface OvalParser {
    ParsedOval parse(InputStream inputStream);
}