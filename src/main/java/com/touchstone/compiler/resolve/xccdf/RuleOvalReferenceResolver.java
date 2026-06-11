package com.touchstone.compiler.resolve.xccdf;

import com.touchstone.compiler.model.resolved.xccdf.ResolvedProfile;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedRuleOvalRefs;

import java.util.List;

public interface RuleOvalReferenceResolver {
    List<ResolvedRuleOvalRefs> resolve(ResolvedProfile resolvedProfile);
}
