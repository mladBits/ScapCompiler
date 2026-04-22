package com.example.scap.resolve.xccdf;

import com.example.scap.index.XccdfBenchmarkIndex;
import com.example.scap.index.XccdfBenchmarkIndexBuilder;
import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfGroup;
import com.example.scap.model.parsed.xccdf.ParsedXccdfProfile;
import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ProfileResolverImpl implements ProfileResolver {

    private final XccdfBenchmarkIndexBuilder indexBuilder;

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
        final XccdfBenchmarkIndex index = indexBuilder.build(benchmark);
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
                              final XccdfBenchmarkIndex index,
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
