package com.example.scap.resolve.xccdf;

import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RuleOvalReferenceResolverImpl implements RuleOvalReferenceResolver {
    private final CheckReferenceCollector checkReferenceCollector;

    @Override
    public List<ResolvedRuleOvalRefs> resolve(final ResolvedProfile resolvedProfile) {
        return resolvedProfile.getSelectedRules().stream()
                .map(this::resolveRule)
                .toList();
    }

    private ResolvedRuleOvalRefs resolveRule(final ResolvedXccdfRule rule) {
        return new ResolvedRuleOvalRefs(
                rule.getRuleId(),
                checkReferenceCollector.collect(rule.getCheckNodes())
        );
    }
}
