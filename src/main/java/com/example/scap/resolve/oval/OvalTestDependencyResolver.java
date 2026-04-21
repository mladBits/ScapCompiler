package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;

import java.util.Collection;
import java.util.List;

public interface OvalTestDependencyResolver {
    Result resolve(OvalIndex ovalIndex, Collection<ParsedOvalTest> tests);

    record Result(
            List<ParsedOvalObject> objects,
            List<ParsedOvalState> states
    ){}
}
