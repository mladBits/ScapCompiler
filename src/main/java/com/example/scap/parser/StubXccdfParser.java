package com.example.scap.parser;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StubXccdfParser implements XccdfParser {

    @Override
    public ParsedXccdf parse(final byte[] xmlBytes) {
        return new ParsedXccdf(
                "benchmark-placeholder",
                List.of("level-1-server"),
                List.of(
                        new ParsedValue(
                                "xccdf_org.example_value_var_password_minlen",
                                "Minimum Password Length",
                                "number",
                                "14"
                        )
                )
        );
    }
}