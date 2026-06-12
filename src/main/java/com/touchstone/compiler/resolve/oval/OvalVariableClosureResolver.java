package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalVariableClosure;

public interface OvalVariableClosureResolver {

    /**
     * Computes the transitive closure of variables referenced by the evaluation
     * slice, including variables referenced from filter-only states and from
     * other local variables, plus the set of objects that local variables
     * reference via object components and therefore need collection plans.
     */
    ResolvedOvalVariableClosure resolve(OvalIndex ovalIndex, ResolvedOvalEvaluationSlice slice);
}