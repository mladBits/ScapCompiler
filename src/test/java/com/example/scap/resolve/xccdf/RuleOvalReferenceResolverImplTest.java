package com.example.scap.resolve.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedComplexCheck;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import com.example.scap.model.resolved.xccdf.ResolvedXccdfRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleOvalReferenceResolverImplTest {
    private final RuleOvalReferenceResolver resolver = new RuleOvalReferenceResolverImpl(new CheckReferenceCollector());

    @Test
    void resolve_shouldReturnEmptyList_whenResolvedProfileHasNoRules() {
        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of());

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void resolve_shouldResolveSingleRuleWithSingleCheckLeaf() {
        ResolvedXccdfRule rule = resolvedRule(
                "rule-1",
                "Rule One",
                checkRef("system-1", "oval-1.xml", "def-1")
        );
        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(1, result.size());

        ResolvedRuleOvalRefs ruleRefs = result.getFirst();
        assertEquals("rule-1", ruleRefs.getRuleId());
        assertEquals(1, ruleRefs.getReferences().size());

        ResolvedCheckReference ref = ruleRefs.getReferences().getFirst();
        assertEquals("system-1", ref.getSystem());
        assertEquals("oval-1.xml", ref.getHref());
        assertEquals("def-1", ref.getName());
    }

    @Test
    void resolve_shouldResolveMultipleRules_preservingRuleOrder() {
        ResolvedXccdfRule rule1 = resolvedRule(
                "rule-1",
                "Rule One",
                checkRef("system-1", "oval-1.xml", "def-1")
        );
        ResolvedXccdfRule rule2 = resolvedRule(
                "rule-2",
                "Rule Two",
                checkRef("system-2", "oval-2.xml", "def-2")
        );

        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule1, rule2));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(2, result.size());

        assertEquals("rule-1", result.get(0).getRuleId());
        assertEquals("rule-2", result.get(1).getRuleId());

        assertResolved(result.get(0).getReferences().getFirst(), "system-1", "oval-1.xml", "def-1");
        assertResolved(result.get(1).getReferences().getFirst(), "system-2", "oval-2.xml", "def-2");
    }

    @Test
    void resolve_shouldFlattenNestedComplexChecksForRule() {
        ParsedCheckReference leaf1 = checkRef("system-1", "oval-1.xml", "def-1");
        ParsedCheckReference leaf2 = checkRef("system-2", "oval-2.xml", "def-2");
        ParsedCheckReference leaf3 = checkRef("system-3", "oval-3.xml", "def-3");

        ParsedComplexCheck nested = complexCheck("OR", leaf2, leaf3);
        ParsedComplexCheck root = complexCheck("AND", leaf1, nested);

        ResolvedXccdfRule rule = resolvedRule("rule-1", "Rule One", root);
        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(1, result.size());
        assertEquals(3, result.getFirst().getReferences().size());

        assertResolved(result.getFirst().getReferences().get(0), "system-1", "oval-1.xml", "def-1");
        assertResolved(result.getFirst().getReferences().get(1), "system-2", "oval-2.xml", "def-2");
        assertResolved(result.getFirst().getReferences().get(2), "system-3", "oval-3.xml", "def-3");
    }

    @Test
    void resolve_shouldDeduplicateReferencesWithinSingleRule() {
        ParsedCheckReference first = checkRef("system-1", "oval.xml", "def-1");
        ParsedCheckReference duplicate = checkRef("system-1", "oval.xml", "def-1");
        ParsedCheckReference second = checkRef("system-2", "oval-2.xml", "def-2");

        ParsedComplexCheck root = complexCheck("AND", first, duplicate, second);

        ResolvedXccdfRule rule = resolvedRule("rule-1", "Rule One", root);
        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().getReferences().size());

        assertResolved(result.getFirst().getReferences().get(0), "system-1", "oval.xml", "def-1");
        assertResolved(result.getFirst().getReferences().get(1), "system-2", "oval-2.xml", "def-2");
    }

    @Test
    void resolve_shouldKeepReferencesSeparatedPerRule() {
        ResolvedXccdfRule rule1 = resolvedRule(
                "rule-1",
                "Rule One",
                checkRef("system-1", "shared.xml", "def-shared")
        );
        ResolvedXccdfRule rule2 = resolvedRule(
                "rule-2",
                "Rule Two",
                checkRef("system-1", "shared.xml", "def-shared")
        );

        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule1, rule2));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getReferences().size());
        assertEquals(1, result.get(1).getReferences().size());

        assertResolved(result.get(0).getReferences().getFirst(), "system-1", "shared.xml", "def-shared");
        assertResolved(result.get(1).getReferences().getFirst(), "system-1", "shared.xml", "def-shared");
    }

    @Test
    void resolve_shouldReturnEmptyReferences_whenRuleHasNoCheckNodes() {
        ResolvedXccdfRule rule = new ResolvedXccdfRule("rule-1", "Rule Without Checks", Collections.emptyList());
        ResolvedProfile resolvedProfile =
                new ResolvedProfile("benchmark-1", "profile-1", List.of(rule));

        List<ResolvedRuleOvalRefs> result = resolver.resolve(resolvedProfile);

        assertEquals(1, result.size());
        assertEquals("rule-1", result.getFirst().getRuleId());
        assertNotNull(result.getFirst().getReferences());
        assertTrue(result.getFirst().getReferences().isEmpty());
    }

    private ResolvedXccdfRule resolvedRule(String ruleId, String title, Object... nodes) {
        List<ParsedCheckNode> checkNodes = new ArrayList<>();
        for (Object node : nodes) {
            checkNodes.add((com.example.scap.model.parsed.xccdf.ParsedCheckNode) node);
        }

        return new ResolvedXccdfRule(ruleId, title, checkNodes);
    }

    private ParsedCheckReference checkRef(String system, String href, String name) {
        ParsedCheckReference ref = new ParsedCheckReference();
        ref.setSystem(system);
        ref.setHref(href);
        ref.setName(name);
        return ref;
    }

    private ParsedComplexCheck complexCheck(String operator,
                                            com.example.scap.model.parsed.xccdf.ParsedCheckNode... children) {
        ParsedComplexCheck complex = new ParsedComplexCheck();
        complex.setOperator(operator);
        complex.getChildren().addAll(List.of(children));
        return complex;
    }

    private void assertResolved(ResolvedCheckReference actual, String system, String href, String name) {
        assertEquals(system, actual.getSystem());
        assertEquals(href, actual.getHref());
        assertEquals(name, actual.getName());
    }
}