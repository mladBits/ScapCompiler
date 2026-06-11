package com.touchstone.compiler.resolve.xccdf;

import com.touchstone.compiler.model.resolved.xccdf.ResolvedProfile;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedXccdfRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
