package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;

import java.util.Collection;

public interface OvalEvaluationSliceResolver {
    ResolvedOvalEvaluationSlice resolve(OvalIndex ovalIndex, Collection<String> startingDefinitionIds);
}
