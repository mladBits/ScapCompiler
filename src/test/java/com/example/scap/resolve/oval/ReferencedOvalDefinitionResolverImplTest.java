package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
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
        OvalIndex ovalIndex = new OvalIndex();
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1",
                        ref("system-1", "oval.xml", "oval:def:1"),
                        ref("system-1", "oval.xml", "oval:def:2"))
        );

        ResolvedOvalEvaluationSlice result = resolver.resolve(ovalIndex, ruleRefs);

        assertSame(closureResolver.returnValue, result);
        assertEquals(2, closureResolver.capturedStartingDefinitionIds.size());
        assertTrue(closureResolver.capturedStartingDefinitionIds.contains("oval:def:1"));
        assertTrue(closureResolver.capturedStartingDefinitionIds.contains("oval:def:2"));
    }

    @Test
    void resolve_shouldDeduplicateDefinitionIdsPreservingFirstEncounterOrder() {
        OvalIndex ovalIndex = new OvalIndex();
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

        resolver.resolve(ovalIndex, ruleRefs);

        assertEquals(3, closureResolver.capturedStartingDefinitionIds.size());
        assertTrue(closureResolver.capturedStartingDefinitionIds.contains("oval:def:1"));
        assertTrue(closureResolver.capturedStartingDefinitionIds.contains("oval:def:2"));
        assertTrue(closureResolver.capturedStartingDefinitionIds.contains("oval:def:3"));
    }

    @Test
    void resolve_shouldIgnoreNullAndBlankDefinitionIds() {
        OvalIndex ovalIndex = new OvalIndex();
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

        resolver.resolve(ovalIndex, ruleRefs);

        assertEquals(List.of("oval:def:1"), closureResolver.capturedStartingDefinitionIds);
    }

    @Test
    void resolve_shouldPassEmptyCollectionWhenNoRuleRefsExist() {
        OvalIndex ovalIndex = new OvalIndex();
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        resolver.resolve(ovalIndex, List.of());

        assertNotNull(closureResolver.capturedStartingDefinitionIds);
        assertTrue(closureResolver.capturedStartingDefinitionIds.isEmpty());
    }

    @Test
    void resolve_shouldPassEmptyCollectionWhenRulesHaveNoReferences() {
        OvalIndex ovalIndex = new OvalIndex();
        CapturingClosureResolver closureResolver = new CapturingClosureResolver();
        ReferencedOvalDefinitionResolver resolver =
                new ReferencedOvalDefinitionResolverImpl(closureResolver);

        List<ResolvedRuleOvalRefs> ruleRefs = List.of(
                ruleRefs("rule-1"),
                ruleRefs("rule-2")
        );

        resolver.resolve(ovalIndex, ruleRefs);

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
                new ResolvedOvalEvaluationSlice(List.of(), List.of(), List.of(), List.of());

        private OvalIndex capturedOvalIndex;
        private List<String> capturedStartingDefinitionIds;

        @Override
        public ResolvedOvalEvaluationSlice resolve(OvalIndex index, Collection<String> startingDefinitionIds) {
            this.capturedOvalIndex = index;
            this.capturedStartingDefinitionIds = new ArrayList<>(startingDefinitionIds);
            return returnValue;
        }
    }
}