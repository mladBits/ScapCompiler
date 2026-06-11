package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedRuleOvalRefs;

import java.util.List;

public interface ReferencedOvalDefinitionResolver {
    ResolvedOvalEvaluationSlice resolve(OvalIndex ovalIndex, List<ResolvedRuleOvalRefs> ruleRefs);
}
