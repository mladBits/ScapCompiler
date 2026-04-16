package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleReaderTest {
    private static final XMLInputFactory2 FACTORY =
            (XMLInputFactory2) XMLInputFactory2.newInstance();

    private final RuleReader ruleReader = new RuleReader(new CheckReader());

    @Test
    void readRule_shouldParseIdTitleAndSimpleCheck() throws Exception {
        String xml = """
                <Rule id="rule-1">
                    <title>  My Rule Title  </title>
                    <check system="http://oval.mitre.org/XMLSchema/oval-definitions-5">
                        <check-content-ref href="content-oval.xml" name="oval:org.example:def:1"/>
                    </check>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-1", rule.getRuleId());
        assertEquals("My Rule Title", rule.getTitle());
        assertEquals(1, rule.getCheckReferences().size());

        ParsedCheckReference check =
                assertInstanceOf(ParsedCheckReference.class, rule.getCheckReferences().getFirst());
        assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5", check.getSystem());
        assertEquals("content-oval.xml", check.getHref());
        assertEquals("oval:org.example:def:1", check.getName());
    }

    @Test
    void readRule_shouldUseFirstTitleWhenMultipleTitlesExist() throws Exception {
        String xml = """
                <Rule id="rule-2">
                    <title>First Title</title>
                    <title>Second Title</title>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-2", rule.getRuleId());
        assertEquals("First Title", rule.getTitle());
    }

    @Test
    void readRule_shouldHandleMissingTitle() throws Exception {
        String xml = """
                <Rule id="rule-3">
                    <check system="system-1">
                        <check-content-ref href="a.xml" name="def:a"/>
                    </check>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-3", rule.getRuleId());
        assertNull(rule.getTitle());
        assertEquals(1, rule.getCheckReferences().size());
    }

    @Test
    void readRule_shouldHandleNoChecks() throws Exception {
        String xml = """
                <Rule id="rule-4">
                    <title>Rule Without Checks</title>
                    <description>ignore me</description>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-4", rule.getRuleId());
        assertEquals("Rule Without Checks", rule.getTitle());
        assertTrue(rule.getCheckReferences().isEmpty());
    }

    @Test
    void readRule_shouldParseMultipleTopLevelCheckElements() throws Exception {
        String xml = """
                <Rule id="rule-5">
                    <title>Multi Check Rule</title>
                    <check system="system-a">
                        <check-content-ref href="a.xml" name="def:a"/>
                    </check>
                    <check system="system-b">
                        <check-content-ref href="b.xml" name="def:b"/>
                    </check>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-5", rule.getRuleId());
        assertEquals("Multi Check Rule", rule.getTitle());
        assertEquals(2, rule.getCheckReferences().size());

        ParsedCheckReference check1 =
                assertInstanceOf(ParsedCheckReference.class, rule.getCheckReferences().get(0));
        ParsedCheckReference check2 =
                assertInstanceOf(ParsedCheckReference.class, rule.getCheckReferences().get(1));

        assertEquals("system-a", check1.getSystem());
        assertEquals("a.xml", check1.getHref());
        assertEquals("def:a", check1.getName());

        assertEquals("system-b", check2.getSystem());
        assertEquals("b.xml", check2.getHref());
        assertEquals("def:b", check2.getName());
    }

    @Test
    void readRule_shouldIgnoreUnrelatedElements() throws Exception {
        String xml = """
                <Rule id="rule-7">
                    <warning>ignore me</warning>
                    <version>1</version>
                    <title>Expected Title</title>
                    <fixtext>ignore me too</fixtext>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-7", rule.getRuleId());
        assertEquals("Expected Title", rule.getTitle());
        assertTrue(rule.getCheckReferences().isEmpty());
    }

    @Test
    void readRule_shouldThrowWhenDocumentEndsBeforeRuleCloses() throws Exception {
        String xml = """
                <Rule id="rule-8">
                    <title>Broken Rule</title>
                    <check system="system-1">
                        <check-content-ref href="broken.xml" name="def:broken"/>
                    </check>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        assertThrows(XMLStreamException.class, () -> ruleReader.readRule(reader));
    }

    private XMLStreamReader2 moveToRuleStart(String xml) throws Exception {
        ByteArrayInputStream in =
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && "Rule".equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("No Rule element found in test XML");
    }
}