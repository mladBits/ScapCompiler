package com.example.scap.parser;

public interface CpeParser {
    ParsedCpe parse(byte[] xmlBytes);

    record ParsedCpe(String source) {
    }
}
