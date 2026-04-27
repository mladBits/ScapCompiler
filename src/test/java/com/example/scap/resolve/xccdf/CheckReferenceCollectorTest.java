package com.example.scap.resolve.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedComplexCheck;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckReferenceCollectorTest {
    private final CheckReferenceCollector collector = new CheckReferenceCollector();

    @Test
    void collect_shouldReturnSingleResolvedReference_forLeafNode() {
        ParsedCheckReference leaf = checkRef(
                "http://oval.mitre.org/XMLSchema/oval-definitions-5",
                "windows-11-oval.xml",
                "oval:example:def:1"
        );

        List<ResolvedCheckReference> result = collector.collect(leaf);

        assertEquals(1, result.size());

        ResolvedCheckReference resolved = result.getFirst();
        assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5", resolved.getSystem());
        assertEquals("windows-11-oval.xml", resolved.getHref());
        assertEquals("oval:example:def:1", resolved.getName());
    }

    @Test
    void collect_shouldFlattenNestedComplexCheckTree_preservingEncounterOrder() {
        ParsedCheckReference leaf1 = checkRef("system-1", "file-1.xml", "def-1");
        ParsedCheckReference leaf2 = checkRef("system-2", "file-2.xml", "def-2");
        ParsedCheckReference leaf3 = checkRef("system-3", "file-3.xml", "def-3");

        ParsedComplexCheck nested = complexCheck("OR", leaf2, leaf3);
        ParsedComplexCheck root = complexCheck("AND", leaf1, nested);

        List<ResolvedCheckReference> result = collector.collect(root);

        assertEquals(3, result.size());

        assertResolved(result.get(0), "system-1", "file-1.xml", "def-1");
        assertResolved(result.get(2), "system-2", "file-2.xml", "def-2");
        assertResolved(result.get(1), "system-3", "file-3.xml", "def-3");
    }

    @Test
    void collect_shouldDeduplicateDuplicateReferences_forSingleNodeInput() {
        ParsedCheckReference leaf1 = checkRef("system-1", "file.xml", "def-1");
        ParsedCheckReference leaf2 = checkRef("system-1", "file.xml", "def-1");

        ParsedComplexCheck root = complexCheck("AND", leaf1, leaf2);

        List<ResolvedCheckReference> result = collector.collect(root);

        assertEquals(1, result.size());
        assertResolved(result.getFirst(), "system-1", "file.xml", "def-1");
    }

    @Test
    void collect_shouldDeduplicateAcrossListInput_preservingFirstEncounterOrder() {
        ParsedCheckReference first = checkRef("system-1", "file-1.xml", "def-1");
        ParsedCheckReference duplicate = checkRef("system-1", "file-1.xml", "def-1");
        ParsedCheckReference second = checkRef("system-2", "file-2.xml", "def-2");

        ParsedComplexCheck nested = complexCheck("AND", duplicate, second);

        List<ResolvedCheckReference> result = collector.collect(List.of(first, nested));

        assertEquals(2, result.size());
        assertResolved(result.get(0), "system-1", "file-1.xml", "def-1");
        assertResolved(result.get(1), "system-2", "file-2.xml", "def-2");
    }

    @Test
    void collect_shouldTreatNullFieldsAsPartOfDeduplicationKey() {
        ParsedCheckReference first = checkRef(null, null, "def-1");
        ParsedCheckReference duplicate = checkRef(null, null, "def-1");
        ParsedCheckReference distinct = checkRef(null, "other.xml", "def-1");

        ParsedComplexCheck root = complexCheck("AND", first, duplicate, distinct);

        List<ResolvedCheckReference> result = collector.collect(root);

        assertEquals(2, result.size());
        assertResolved(result.get(0), "", "other.xml", "def-1");
        assertResolved(result.get(1), "", "", "def-1");
    }

    @Test
    void collect_shouldReturnEmptyList_forEmptyNodeList() {
        List<ResolvedCheckReference> result = collector.collect(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_shouldIgnoreUnknownParsedCheckNodeType() {
        ParsedCheckNode unknown = new ParsedCheckNode() {
        };

        List<ResolvedCheckReference> result = collector.collect(unknown);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private ParsedCheckReference checkRef(String system, String href, String name) {
        ParsedCheckReference ref = new ParsedCheckReference();
        ref.setSystem(system);
        ref.setHref(href);
        ref.setName(name);
        return ref;
    }

    private ParsedComplexCheck complexCheck(String operator, ParsedCheckNode... children) {
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