package com.example.scap.resolve.oval;

import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;

import java.util.List;

public interface ReferencedOvalDefinitionResolver {
    ResolvedOvalEvaluationSlice resolve(List<ResolvedRuleOvalRefs> ruleRefs);
}
