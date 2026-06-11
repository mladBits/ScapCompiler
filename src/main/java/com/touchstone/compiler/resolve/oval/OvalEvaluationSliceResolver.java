package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;

import java.util.Collection;

public interface OvalEvaluationSliceResolver {
    ResolvedOvalEvaluationSlice resolve(OvalIndex ovalIndex, Collection<String> startingDefinitionIds);
}
