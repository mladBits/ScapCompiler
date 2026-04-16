package com.example.scap.resolve.xccdf;

import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;

import java.util.List;

public interface RuleOvalReferenceResolver {
    List<ResolvedRuleOvalRefs> resolve(ResolvedProfile resolvedProfile);
}
