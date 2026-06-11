package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObjectBase;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;

import java.util.Collection;
import java.util.List;

public interface OvalTestDependencyResolver {
    Result resolve(OvalIndex ovalIndex, Collection<ParsedOvalTest> tests);

    record Result(List<ParsedOvalObjectBase> objects, List<ParsedOvalState> states){}
}
