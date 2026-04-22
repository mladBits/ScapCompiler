package com.example.scap.oval;

import com.example.scap.model.parsed.oval.ParsedOvalTest;

import java.util.Optional;

public interface OvalCheckCompiler<T extends CompiledOvalCheck> {
    boolean supports(ParsedOvalTest test);
    Optional<T> compile(OvalCheckCompileContext context,
                    ParsedOvalTest test);
}
