package com.example.scap.parser;

import com.example.scap.model.parsed.oval.ParsedOval;

import java.io.InputStream;

public interface OvalParser {
    ParsedOval parse(InputStream inputStream);
}