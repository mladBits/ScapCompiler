package com.touchstone.compiler.parser;

public interface CpeParser {
    ParsedCpe parse(byte[] xmlBytes);

    record ParsedCpe(String source) {
    }
}
