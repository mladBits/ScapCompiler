package com.example.scap.resolve.oval;

import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferencedOvalDefinitionResolverImplTest {
    @Test
    void resolve_shouldExtractDefinitionIdsFromRuleOvalRefs() {
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1",
                        ref("system-1", "oval.xml", "oval:def:1"),
                        ref("system-1", "oval.xml", "oval:def:2"))
        );

        ResolvedOvalEvaluationSlice result = resolver.resolve(ruleRefs);

        assertSame(closureResolver.returnValue, result);
        assertEquals(List.of("oval:def:1", "oval:def:2"), closureResolver.capturedStartingDefinitionIds);
    }

    @Test
    void resolve_shouldDeduplicateDefinitionIdsPreservingFirstEncounterOrder() {
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1",
                        ref("system-1", "oval.xml", "oval:def:1"),
                        ref("system-1", "oval.xml", "oval:def:2")),
                ruleRefs("rule-2",
                        ref("system-1", "oval.xml", "oval:def:1"),
                        ref("system-1", "oval.xml", "oval:def:3"))
        );

        resolver.resolve(ruleRefs);

        assertEquals(
                List.of("oval:def:1", "oval:def:2", "oval:def:3"),
                closureResolver.capturedStartingDefinitionIds
        );
    }

    @Test
    void resolve_shouldIgnoreNullAndBlankDefinitionIds() {
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1",
                        ref("system-1", "oval.xml", null),
                        ref("system-1", "oval.xml", ""),
                        ref("system-1", "oval.xml", "   "),
                        ref("system-1", "oval.xml", "oval:def:1"))
        );

        resolver.resolve(ruleRefs);

        assertEquals(List.of("oval:def:1"), closureResolver.capturedStartingDefinitionIds);
    }

    @Test
    void resolve_shouldPassEmptyCollectionWhenNoRuleRefsExist() {
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        resolver.resolve(List.of());

        assertNotNull(closureResolver.capturedStartingDefinitionIds);
        assertTrue(closureResolver.capturedStartingDefinitionIds.isEmpty());
    }

    @Test
    void resolve_shouldPassEmptyCollectionWhenRulesHaveNoReferences() {
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1"),
                ruleRefs("rule-2")
        );

        resolver.resolve(ruleRefs);

        assertNotNull(closureResolver.capturedStartingDefinitionIds);
        assertTrue(closureResolver.capturedStartingDefinitionIds.isEmpty());
    }

    private ResolvedRuleOvalRefs ruleRefs(String ruleId, ResolvedCheckReference... references) {
        return new ResolvedRuleOvalRefs(ruleId, new ArrayList<>(List.of(references)));
    }

    private ResolvedCheckReference ref(String system, String href, String name) {
        return new ResolvedCheckReference(system, href, name);
    }

    private static class CapturingClosureResolver implements OvalDefinitionClosureResolver {
        private final ResolvedOvalEvaluationSlice returnValue =
                new ResolvedOvalEvaluationSlice(List.of(), List.of());

        private List<String> capturedStartingDefinitionIds;

        @Override
        public ResolvedOvalEvaluationSlice resolve(Collection<String> startingDefinitionIds) {
            this.capturedStartingDefinitionIds = new ArrayList<>(startingDefinitionIds);
            return returnValue;
        }
    }
}