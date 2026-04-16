package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuleReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final RuleReader ruleReader = new RuleReader();

    @Test
    void readRule_shouldParseIdAndTitle() throws Exception {
        String xml = """
                <Rule id="rule-1">
                    <title>  My Rule Title  </title>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-1", rule.getRuleId());
        assertEquals("My Rule Title", rule.getTitle());
    }

    @Test
    void readRule_shouldLeaveTitleNullWhenMissing() throws Exception {
        String xml = """
                <Rule id="rule-2">
                    <description>No title here</description>
                </Rule>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-2", rule.getRuleId());
        assertNull(rule.getTitle());
    }

    @Test
    void readRule_shouldThrowWhenDocumentEndsBeforeRuleCloses() throws Exception {
        String xml = """
                <Rule id="rule-3">
                    <title>Broken Rule</title>
                """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        XMLStreamException ex = assertThrows(
                XMLStreamException.class,
                () -> ruleReader.readRule(reader)
        );

        // Parser may throw either your custom message or a lower-level XML parsing message.
        assertNotNull(ex.getMessage());
    }

    @Test
    void readRule_shouldUseFirstTitleOnly() throws Exception {
        String xml = """
            <Rule id="rule-4">
                <title>First Title</title>
                <title>Second Title</title>
            </Rule>
            """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-4", rule.getRuleId());
        assertEquals("First Title", rule.getTitle());
    }

    @Test
    void readRule_shouldIgnoreUnrelatedElements() throws Exception {
        String xml = """
            <Rule id="rule-5">
                <version>1</version>
                <warning>ignore me</warning>
                <title>Expected Title</title>
            </Rule>
            """;

        XMLStreamReader2 reader = moveToRuleStart(xml);

        ParsedXccdfRule rule = ruleReader.readRule(reader);

        assertEquals("rule-5", rule.getRuleId());
        assertEquals("Expected Title", rule.getTitle());
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