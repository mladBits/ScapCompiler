package com.example.scap.oval;

import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.oval.common.CompiledOvalCheckBase;

import java.util.Optional;

public interface OvalCheckCompiler {
    boolean supports(ParsedOvalTest test);
    Optional<CompiledOvalCheckBase> compile(OvalCheckCompileContext context,
                                            ParsedOvalTest test);
}
