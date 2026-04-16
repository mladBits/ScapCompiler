package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedComplexCheck;
import com.example.scap.parser.reader.xccdf.CheckReader;
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

class CheckReaderTest {

    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final CheckReader checkReader = new CheckReader();

    @Test
    void readCheckNode_shouldParseSimpleCheckLeaf() throws Exception {
        String xml = """
                <check system="http://oval.mitre.org/XMLSchema/oval-definitions-5">
                    <check-content-ref href="content-oval.xml" name="oval:org.example:def:1"/>
                </check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "check");

        ParsedCheckNode node = checkReader.readCheckNode(reader);

        ParsedCheckReference check = assertInstanceOf(ParsedCheckReference.class, node);
        assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5", check.getSystem());
        assertEquals("content-oval.xml", check.getHref());
        assertEquals("oval:org.example:def:1", check.getName());
    }

    @Test
    void readCheckNode_shouldParseSimpleCheckLeafWithoutCheckContentRef() throws Exception {
        String xml = """
                <check system="system-1">
                    <note>ignore me</note>
                </check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "check");

        ParsedCheckNode node = checkReader.readCheckNode(reader);

        ParsedCheckReference check = assertInstanceOf(ParsedCheckReference.class, node);
        assertEquals("system-1", check.getSystem());
        assertNull(check.getHref());
        assertNull(check.getName());
    }

    @Test
    void readCheckNode_shouldParseComplexCheckWithDefaultAndOperator() throws Exception {
        String xml = """
                <complex-check>
                    <check system="system-a">
                        <check-content-ref href="a.xml" name="def:a"/>
                    </check>
                    <check system="system-b">
                        <check-content-ref href="b.xml" name="def:b"/>
                    </check>
                </complex-check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "complex-check");

        ParsedCheckNode node = checkReader.readCheckNode(reader);

        ParsedComplexCheck complex = assertInstanceOf(ParsedComplexCheck.class, node);
        assertEquals("AND", complex.getOperator());
        assertEquals(2, complex.getChildren().size());

        ParsedCheckReference child1 =
                assertInstanceOf(ParsedCheckReference.class, complex.getChildren().get(0));
        ParsedCheckReference child2 =
                assertInstanceOf(ParsedCheckReference.class, complex.getChildren().get(1));

        assertEquals("system-a", child1.getSystem());
        assertEquals("a.xml", child1.getHref());
        assertEquals("def:a", child1.getName());

        assertEquals("system-b", child2.getSystem());
        assertEquals("b.xml", child2.getHref());
        assertEquals("def:b", child2.getName());
    }

    @Test
    void readCheckNode_shouldParseComplexCheckWithNestedComplexCheck() throws Exception {
        String xml = """
                <complex-check operator="OR">
                    <check system="system-root">
                        <check-content-ref href="root.xml" name="def:root"/>
                    </check>
                    <complex-check operator="XOR">
                        <check system="system-nested-1">
                            <check-content-ref href="nested-1.xml" name="def:nested1"/>
                        </check>
                        <check system="system-nested-2">
                            <check-content-ref href="nested-2.xml" name="def:nested2"/>
                        </check>
                    </complex-check>
                </complex-check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "complex-check");

        ParsedCheckNode node = checkReader.readCheckNode(reader);

        ParsedComplexCheck root = assertInstanceOf(ParsedComplexCheck.class, node);
        assertEquals("OR", root.getOperator());
        assertEquals(2, root.getChildren().size());

        ParsedCheckReference rootLeaf =
                assertInstanceOf(ParsedCheckReference.class, root.getChildren().get(0));
        assertEquals("system-root", rootLeaf.getSystem());
        assertEquals("root.xml", rootLeaf.getHref());
        assertEquals("def:root", rootLeaf.getName());

        ParsedComplexCheck nested =
                assertInstanceOf(ParsedComplexCheck.class, root.getChildren().get(1));
        assertEquals("XOR", nested.getOperator());
        assertEquals(2, nested.getChildren().size());

        ParsedCheckReference nested1 =
                assertInstanceOf(ParsedCheckReference.class, nested.getChildren().get(0));
        ParsedCheckReference nested2 =
                assertInstanceOf(ParsedCheckReference.class, nested.getChildren().get(1));

        assertEquals("system-nested-1", nested1.getSystem());
        assertEquals("nested-1.xml", nested1.getHref());
        assertEquals("def:nested1", nested1.getName());

        assertEquals("system-nested-2", nested2.getSystem());
        assertEquals("nested-2.xml", nested2.getHref());
        assertEquals("def:nested2", nested2.getName());
    }

    @Test
    void readCheckNode_shouldParseComplexCheckContainingOnlyNestedComplexCheck() throws Exception {
        String xml = """
                <complex-check operator="AND">
                    <complex-check operator="OR">
                        <check system="system-1">
                            <check-content-ref href="one.xml" name="def:one"/>
                        </check>
                    </complex-check>
                </complex-check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "complex-check");

        ParsedCheckNode node = checkReader.readCheckNode(reader);

        ParsedComplexCheck root = assertInstanceOf(ParsedComplexCheck.class, node);
        assertEquals("AND", root.getOperator());
        assertEquals(1, root.getChildren().size());

        ParsedComplexCheck nested =
                assertInstanceOf(ParsedComplexCheck.class, root.getChildren().get(0));
        assertEquals("OR", nested.getOperator());
        assertEquals(1, nested.getChildren().size());

        ParsedCheckReference leaf =
                assertInstanceOf(ParsedCheckReference.class, nested.getChildren().get(0));
        assertEquals("system-1", leaf.getSystem());
        assertEquals("one.xml", leaf.getHref());
        assertEquals("def:one", leaf.getName());
    }

    @Test
    void readCheckNode_shouldThrowForUnsupportedElement() throws Exception {
        String xml = """
                <foo/>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "foo");

        XMLStreamException ex = assertThrows(
                XMLStreamException.class,
                () -> checkReader.readCheckNode(reader)
        );

        assertTrue(ex.getMessage().contains("Unsupported check node"));
    }

    @Test
    void readCheckNode_shouldThrowWhenCheckDoesNotClose() throws Exception {
        String xml = """
                <check system="system-1">
                    <check-content-ref href="broken.xml" name="def:broken">
                """;

        XMLStreamReader2 reader = moveToStart(xml, "check");

        assertThrows(XMLStreamException.class, () -> checkReader.readCheckNode(reader));
    }

    @Test
    void readCheckNode_shouldThrowWhenComplexCheckDoesNotClose() throws Exception {
        String xml = """
                <complex-check operator="AND">
                    <check system="system-1">
                        <check-content-ref href="broken.xml" name="def:broken"/>
                    </check>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "complex-check");

        assertThrows(XMLStreamException.class, () -> checkReader.readCheckNode(reader));
    }

    private XMLStreamReader2 moveToStart(String xml, String elementName) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT && elementName.equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("No " + elementName + " element found in test XML");
    }
}