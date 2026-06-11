package com.touchstone.compiler.resolve;

import com.touchstone.compiler.index.XccdfIndexBuilder;
import com.touchstone.compiler.model.parsed.xccdf.*;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedProfile;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedXccdfRule;
import com.touchstone.compiler.parser.XccdfParser;
import com.touchstone.compiler.parser.XccdfParserImpl;
import com.touchstone.compiler.parser.reader.xccdf.*;
import com.touchstone.compiler.resolve.xccdf.ProfileResolver;
import com.touchstone.compiler.resolve.xccdf.ProfileResolverImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileResolverImplTest {
    private static final String resourceName = "xccdf.xml";
    private final ProfileResolver resolver = new ProfileResolverImpl(new XccdfIndexBuilder());

    @Test
    void resolve_shouldResolveDirectRuleSelection() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfRule rule1 = rule("rule-1", "Rule One");
        ParsedXccdfRule rule2 = rule("rule-2", "Rule Two");
        benchmark.getRules().add(rule1);
        benchmark.getRules().add(rule2);

        ParsedXccdfProfile profile = profile("profile-1", "rule-2");
        benchmark.getProfiles().add(profile);

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");

        assertEquals("benchmark-1", resolved.getBenchmarkId());
        assertEquals("profile-1", resolved.getProfileId());
        assertEquals(1, resolved.getSelectedRules().size());

        ResolvedXccdfRule resolvedRule = resolved.getSelectedRules().getFirst();
        assertEquals("rule-2", resolvedRule.getRuleId());
        assertEquals("Rule Two", resolvedRule.getTitle());
    }

    @Test
    void resolve_shouldResolveDirectGroupSelection() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfGroup group = group("group-1");
        ParsedXccdfRule rule1 = rule("rule-1", "Rule One");
        ParsedXccdfRule rule2 = rule("rule-2", "Rule Two");
        group.getRules().add(rule1);
        group.getRules().add(rule2);
        benchmark.getGroups().add(group);

        ParsedXccdfProfile profile = profile("profile-1", "group-1");
        benchmark.getProfiles().add(profile);

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");

        assertEquals(2, resolved.getSelectedRules().size());
        assertEquals(List.of("rule-1", "rule-2"),
                resolved.getSelectedRules().stream().map(ResolvedXccdfRule::getRuleId).toList());
    }

    @Test
    void resolve_shouldResolveNestedGroupSelection() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfGroup parent = group("group-parent");
        ParsedXccdfGroup child = group("group-child");
        ParsedXccdfGroup grandchild = group("group-grandchild");

        ParsedXccdfRule childRule = rule("rule-child", "Child Rule");
        ParsedXccdfRule grandchildRule = rule("rule-grandchild", "Grandchild Rule");

        child.getRules().add(childRule);
        grandchild.getRules().add(grandchildRule);
        child.getGroups().add(grandchild);
        parent.getGroups().add(child);
        benchmark.getGroups().add(parent);

        ParsedXccdfProfile profile = profile("profile-1", "group-parent");
        benchmark.getProfiles().add(profile);

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");

        assertEquals(2, resolved.getSelectedRules().size());
        assertEquals(List.of("rule-child", "rule-grandchild"),
                resolved.getSelectedRules().stream().map(ResolvedXccdfRule::getRuleId).toList());
    }

    @Test
    void resolve_shouldDeduplicateRulesWhenSelectedDirectlyAndViaGroup() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfRule rule1 = rule("rule-1", "Rule One");
        benchmark.getRules().add(rule1);

        ParsedXccdfGroup group = group("group-1");
        group.getRules().add(rule1);
        benchmark.getGroups().add(group);

        ParsedXccdfProfile profile = profile("profile-1", "group-1", "rule-1");
        benchmark.getProfiles().add(profile);

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");

        assertEquals(1, resolved.getSelectedRules().size());
        assertEquals("rule-1", resolved.getSelectedRules().getFirst().getRuleId());
    }

    @Test
    void resolve_shouldPreserveEncounterOrderFromProfileSelections() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfRule rule1 = rule("rule-1", "Rule One");
        ParsedXccdfRule rule2 = rule("rule-2", "Rule Two");
        ParsedXccdfRule rule3 = rule("rule-3", "Rule Three");

        ParsedXccdfGroup group = group("group-1");
        group.getRules().add(rule2);
        group.getRules().add(rule3);

        benchmark.getRules().add(rule1);
        benchmark.getGroups().add(group);

        ParsedXccdfProfile profile = profile("profile-1", "rule-1", "group-1");
        benchmark.getProfiles().add(profile);

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");

        assertEquals(List.of("rule-1", "rule-2", "rule-3"),
                resolved.getSelectedRules().stream().map(ResolvedXccdfRule::getRuleId).toList());
    }

    @Test
    void resolve_shouldThrowWhenProfileNotFound() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(benchmark, "missing-profile")
        );

        assertEquals("Profile not found: missing-profile", ex.getMessage());
    }

    @Test
    void resolve_shouldThrowWhenSelectedIdRefCannotBeResolved() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");
        benchmark.getProfiles().add(profile("profile-1", "does-not-exist"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(benchmark, "profile-1")
        );

        assertEquals("Unresolved profile selection idref: does-not-exist", ex.getMessage());
    }

    @Test
    void resolve_shouldCopyCheckReferencesIntoResolvedRules() {
        ParsedXccdfBenchmark benchmark = benchmark("benchmark-1");

        ParsedXccdfRule rule = rule("rule-1", "Rule One");
        ParsedCheckReference checkReference = new ParsedCheckReference();
        checkReference.setSystem("system-1");
        checkReference.setHref("oval.xml");
        checkReference.setName("oval:def:1");
        rule.getCheckReferences().add(checkReference);

        benchmark.getRules().add(rule);
        benchmark.getProfiles().add(profile("profile-1", "rule-1"));

        ResolvedProfile resolved = resolver.resolve(benchmark, "profile-1");
        ResolvedXccdfRule resolvedRule = resolved.getSelectedRules().getFirst();

        assertEquals(1, resolvedRule.getCheckNodes().size());

        rule.getCheckReferences().clear();

        assertEquals(0, rule.getCheckReferences().size());
        assertEquals(1, resolvedRule.getCheckNodes().size());
    }

    @Test
    void testResolve()
            throws IOException {
        final String profileId = "xccdf_mil.disa.stig_profile_MAC-1_Classified";
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(in);

            final RuleReader ruleReader = new RuleReader(new CheckReader());
            final XccdfParser parser = new XccdfParserImpl(
                    new BenchmarkReader(
                            new ProfileReader(),
                            ruleReader,
                            new GroupReader(ruleReader),
                            new ValueReader()));

            final ParsedXccdfBenchmark parsedXccdfBenchmark = parser.parse(in);
            final XccdfIndexBuilder indexBuilder = new XccdfIndexBuilder();
            final ProfileResolver resolver = new ProfileResolverImpl(indexBuilder);
            final ResolvedProfile resolvedProfile = resolver.resolve(parsedXccdfBenchmark, profileId);
            assertEquals("xccdf_mil.disa.stig_benchmark_Microsoft_Windows_11_STIG", resolvedProfile.getBenchmarkId());
            assertEquals(profileId, resolvedProfile.getProfileId());
            assertEquals(223, resolvedProfile.getSelectedRules().size());
        }
    }

    private ParsedXccdfBenchmark benchmark(String benchmarkId) {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        benchmark.setBenchmarkId(benchmarkId);
        return benchmark;
    }

    private ParsedXccdfProfile profile(String profileId, String... selectedIdRefs) {
        ParsedXccdfProfile profile = new ParsedXccdfProfile();
        profile.setProfileId(profileId);
        profile.getSelectedIdRefs().addAll(List.of(selectedIdRefs));
        return profile;
    }

    private ParsedXccdfGroup group(String groupId) {
        ParsedXccdfGroup group = new ParsedXccdfGroup();
        group.setGroupId(groupId);
        return group;
    }

    private ParsedXccdfRule rule(String ruleId, String title) {
        ParsedXccdfRule rule = new ParsedXccdfRule();
        rule.setRuleId(ruleId);
        rule.setTitle(title);
        return rule;
    }

}