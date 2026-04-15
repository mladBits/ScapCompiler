package com.example.scap.parser;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StubOvalParser implements OvalParser {

    @Override
    public ParsedOval parse(final byte[] xmlBytes) {
        return new ParsedOval(
                List.of("oval:org.example:def:1"),
                List.of(new ParsedExternalVariable("oval:org.example:var:1", "int"))
        );
    }
}
