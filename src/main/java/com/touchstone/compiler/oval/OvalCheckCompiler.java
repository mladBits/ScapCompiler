package com.touchstone.compiler.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObjectBase;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.oval.common.CompiledOvalCheckBase;

import java.util.Optional;

public interface OvalCheckCompiler {
    boolean supports(ParsedOvalTest test);
    Optional<CompiledOvalCheckBase> compile(OvalCheckCompileContext context,
                                            ParsedOvalTest test);

    /**
     * Whether this compiler can produce a collection plan for the given object
     * outside of a test, e.g. an object referenced only by a local variable's
     * object component.
     */
    boolean supportsObject(ParsedOvalObjectBase object);

    /**
     * Compiles a collection plan for an object that is not reachable through a
     * test (variable object components). Compiled plans are added to the context.
     */
    ObjectCompilationResult compileObjectPlan(OvalCheckCompileContext context,
                                              ParsedOvalObjectBase object);
}