package com.touchstone.compiler.resolve.xccdf;

import com.touchstone.compiler.index.XccdfIndex;
import com.touchstone.compiler.index.XccdfIndexBuilder;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfGroup;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfProfile;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfRule;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedProfile;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedXccdfRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ProfileResolverImpl implements ProfileResolver {

    private final XccdfIndexBuilder indexBuilder;

    @Override
    public ResolvedProfile resolve(final ParsedXccdfBenchmark benchmark, final String profileId) {
        final Optional<ParsedXccdfProfile> profileOpt =
                benchmark.getProfiles().stream()
                        .filter(x -> x.getProfileId().equals(profileId))
                        .findFirst();

        if (profileOpt.isEmpty()) {
            throw new IllegalArgumentException("Profile not found: " + profileId);
        }

        final ParsedXccdfProfile profile = profileOpt.get();
        final XccdfIndex index = indexBuilder.build(benchmark);
        final Set<String> resolvedRuleIds = new LinkedHashSet<>();

        profile.getSelectedIdRefs().forEach(idRef -> resolveIdRef(idRef, index, resolvedRuleIds));

        final List<ResolvedXccdfRule> resolvedRules =
                resolvedRuleIds.stream()
                .filter(ruleId -> index.getRulesById().containsKey(ruleId))
                .map(ruleId -> index.getRulesById().get(ruleId))
                .map(parsedXccdfRule ->
                        new ResolvedXccdfRule(
                                parsedXccdfRule.getRuleId(),
                                parsedXccdfRule.getTitle(),
                                new ArrayList<>(parsedXccdfRule.getCheckReferences())))
                .toList();

        return new ResolvedProfile(
                benchmark.getBenchmarkId(),
                profile.getProfileId(),
                resolvedRules);
    }

    private void resolveGroup(final ParsedXccdfGroup group, final Set<String> resolvedRuleIds) {
        group.getRules().stream()
                .map(ParsedXccdfRule::getRuleId)
                .forEach(resolvedRuleIds::add);

        group.getGroups().forEach(childGroup -> resolveGroup(childGroup, resolvedRuleIds));
    }

    private void resolveIdRef(final String idRef,
                              final XccdfIndex index,
                              final Set<String> resolvedRuleIds) {
        if (index.getRulesById().containsKey(idRef)) {
            resolvedRuleIds.add(idRef);
        } else if (index.getGroupsById().containsKey(idRef)) {
            resolveGroup(index.getGroupsById().get(idRef), resolvedRuleIds);
        } else {
            throw new IllegalArgumentException("Unresolved profile selection idref: " + idRef);
        }
    }
}
