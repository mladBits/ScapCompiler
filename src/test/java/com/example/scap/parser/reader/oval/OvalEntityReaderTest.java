package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalEntityReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final OvalEntityReader entityReader = new OvalEntityReader();

    @Test
    void readEntity_shouldParseElementNameTextAndAttributes() throws Exception {
        String xml = """
                <hive datatype="string" operation="equals">HKEY_LOCAL_MACHINE</hive>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "hive");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("hive", entity.getName());
        assertEquals("HKEY_LOCAL_MACHINE", entity.getValue());
        assertEquals("string", entity.getAttributes().get("datatype"));
        assertEquals("equals", entity.getAttributes().get("operation"));
    }

    @Test
    void readEntity_shouldTrimTextValue() throws Exception {
        String xml = """
                <key>
                    Software\\Microsoft\\Windows
                </key>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "key");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("key", entity.getName());
        assertEquals("Software\\Microsoft\\Windows", entity.getValue());
    }

    @Test
    void readEntity_shouldSetNullValueWhenTextIsBlank() throws Exception {
        String xml = """
                <name>   </name>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "name");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("name", entity.getName());
        assertNull(entity.getValue());
    }

    @Test
    void readEntity_shouldHandleEmptySelfClosingElement() throws Exception {
        String xml = """
                <name datatype="string"/>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "name");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("name", entity.getName());
        assertNull(entity.getValue());
        assertEquals("string", entity.getAttributes().get("datatype"));
    }

    @Test
    void readEntity_shouldSkipNestedElements() throws Exception {
        String xml = """
                <set set_operator="UNION">
                    <object_reference>oval:obj:1</object_reference>
                    <object_reference>oval:obj:2</object_reference>
                </set>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "set");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("set", entity.getName());
        assertEquals("UNION", entity.getAttributes().get("set_operator"));
        assertNull(entity.getValue());
    }

    @Test
    void readEntity_shouldPreserveTextOutsideSkippedNestedElements() throws Exception {
        String xml = """
                <entity>
                    before
                    <nested>ignore me</nested>
                    after
                </entity>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "entity");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("entity", entity.getName());

        // XML whitespace is preserved around text nodes, so assert the meaningful content.
        assertTrue(entity.getValue().contains("before"));
        assertTrue(entity.getValue().contains("after"));
        assertFalse(entity.getValue().contains("ignore me"));
    }

    @Test
    void readEntity_shouldSkipDeeplyNestedElements() throws Exception {
        String xml = """
                <set>
                    <object_reference>
                        <nested>
                            <deeper>ignore me</deeper>
                        </nested>
                    </object_reference>
                </set>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "set");

        ParsedOvalEntity entity = entityReader.readEntity(reader);

        assertEquals("set", entity.getName());
        assertNull(entity.getValue());
    }

    @Test
    void readEntity_shouldThrowWhenEntityDoesNotClose() throws Exception {
        String xml = """
                <hive datatype="string">HKEY_LOCAL_MACHINE
                """;

        XMLStreamReader2 reader = moveToStart(xml, "hive");

        assertThrows(XMLStreamException.class, () -> entityReader.readEntity(reader));
    }

    @Test
    void readEntity_shouldThrowWhenNestedElementDoesNotClose() throws Exception {
        String xml = """
                <set>
                    <object_reference>oval:obj:1
                </set>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "set");

        assertThrows(XMLStreamException.class, () -> entityReader.readEntity(reader));
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

        throw new IllegalStateException("No " + elementName + " element found in test XML");
    }
}