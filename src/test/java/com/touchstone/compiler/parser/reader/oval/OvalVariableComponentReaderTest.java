package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.variables.ParsedConcatComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedLiteralComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedObjectComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariableComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedRegexCaptureComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedUnsupportedComponent;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedVariableComponent;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OvalVariableComponentReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final OvalVariableComponentReader componentReader = new OvalVariableComponentReader();

    @Test
    void readComponent_shouldParseLiteralComponentText() throws Exception {
        String xml = """
                <literal_component>\\Administrator</literal_component>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "literal_component"));

        ParsedLiteralComponent literal = assertInstanceOf(ParsedLiteralComponent.class, component);
        assertEquals("\\Administrator", literal.getValue());
    }

    @Test
    void readComponent_shouldParseVariableComponent() throws Exception {
        String xml = """
                <variable_component var_ref="oval:test:var:1"/>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "variable_component"));

        ParsedVariableComponent variable = assertInstanceOf(ParsedVariableComponent.class, component);
        assertEquals("oval:test:var:1", variable.getVarRef());
    }

    @Test
    void readComponent_shouldParseRegexCaptureOverConcat() throws Exception {
        String xml = """
                <regex_capture pattern="^(.*)$">
                    <concat>
                        <literal_component>a</literal_component>
                        <object_component object_ref="oval:test:obj:1" item_field="value"/>
                    </concat>
                </regex_capture>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "regex_capture"));

        ParsedRegexCaptureComponent regexCapture =
                assertInstanceOf(ParsedRegexCaptureComponent.class, component);
        assertEquals("^(.*)$", regexCapture.getPattern());

        ParsedConcatComponent concat =
                assertInstanceOf(ParsedConcatComponent.class, regexCapture.getComponent());
        assertEquals(2, concat.getComponents().size());
        assertInstanceOf(ParsedLiteralComponent.class, concat.getComponents().get(0));
        assertInstanceOf(ParsedObjectComponent.class, concat.getComponents().get(1));
    }

    @Test
    void readComponent_shouldParseRegexCaptureOverLiteral() throws Exception {
        String xml = """
                <regex_capture pattern="^%.*%(.*)$">
                    <literal_component>%SystemRoot%\\System32</literal_component>
                </regex_capture>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "regex_capture"));

        ParsedRegexCaptureComponent regexCapture =
                assertInstanceOf(ParsedRegexCaptureComponent.class, component);
        ParsedLiteralComponent literal =
                assertInstanceOf(ParsedLiteralComponent.class, regexCapture.getComponent());
        assertEquals("%SystemRoot%\\System32", literal.getValue());
    }

    @Test
    void readComponent_shouldSkipUnsupportedFunctionAndKeepReaderPositioned() throws Exception {
        String xml = """
                <concat>
                    <split delimiter=",">
                        <object_component object_ref="oval:test:obj:1" item_field="value"/>
                    </split>
                    <literal_component>after</literal_component>
                </concat>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "concat"));

        ParsedConcatComponent concat = assertInstanceOf(ParsedConcatComponent.class, component);
        assertEquals(2, concat.getComponents().size());

        ParsedUnsupportedComponent unsupported =
                assertInstanceOf(ParsedUnsupportedComponent.class, concat.getComponents().get(0));
        assertEquals("split", unsupported.getFunctionName());

        // The whole <split> subtree must be consumed so the sibling still parses.
        ParsedLiteralComponent literal =
                assertInstanceOf(ParsedLiteralComponent.class, concat.getComponents().get(1));
        assertEquals("after", literal.getValue());
    }

    @Test
    void readComponent_shouldParseObjectComponentAttributes() throws Exception {
        String xml = """
                <object_component object_ref="oval:test:obj:9" item_field="trustee_sid"/>
                """;

        ParsedOvalVariableComponent component =
                componentReader.readComponent(moveToStart(xml, "object_component"));

        ParsedObjectComponent objectComponent =
                assertInstanceOf(ParsedObjectComponent.class, component);
        assertEquals("oval:test:obj:9", objectComponent.getObjectRef());
        assertEquals("trustee_sid", objectComponent.getItemField());
    }

    private XMLStreamReader2 moveToStart(String xml, String elementName) throws Exception {
        ByteArrayInputStream in =
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && elementName.equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("Element not found: " + elementName);
    }
}