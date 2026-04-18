package com.example.scap.resolve.oval;

import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;

import java.util.Collection;

public interface OvalDefinitionClosureResolver {
    ResolvedOvalEvaluationSlice resolve(Collection<String> startingDefinitionIds);
}
