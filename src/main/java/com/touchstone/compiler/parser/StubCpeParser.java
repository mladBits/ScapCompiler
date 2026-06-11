package com.touchstone.compiler.parser;

import org.springframework.stereotype.Component;

@Component
public class StubCpeParser implements CpeParser {

    @Override
    public ParsedCpe parse(final byte[] xmlBytes) {
        return new ParsedCpe("placeholder");
    }
}