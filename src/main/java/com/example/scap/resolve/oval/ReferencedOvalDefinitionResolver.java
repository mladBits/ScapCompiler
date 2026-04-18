package com.example.scap.resolve.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;

import java.util.List;

public interface ReferencedOvalDefinitionResolver {
    List<ParsedOvalDefinition> resolve(List<ResolvedRuleOvalRefs> ruleRefs);
}
