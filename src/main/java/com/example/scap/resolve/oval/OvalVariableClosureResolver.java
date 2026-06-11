package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.resolved.oval.ResolvedOvalVariableClosure;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;

public interface OvalVariableClosureResolver {

    /**
     * Computes the transitive closure of variables referenced by the evaluation
     * slice, including variables referenced from filter-only states and from
     * other local variables, plus the set of objects that local variables
     * reference via object components and therefore need collection plans.
     */
    ResolvedOvalVariableClosure resolve(OvalIndex ovalIndex, ResolvedOvalEvaluationSlice slice);
}