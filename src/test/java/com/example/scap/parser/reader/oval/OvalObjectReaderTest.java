package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalObjectReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final OvalObjectReader objectReader = new OvalObjectReader(new OvalEntityReader());

    @Test
    void readObject_shouldParseSingleObjectWithEntities() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1" version="1">
                        <win:hive datatype="string">HKEY_LOCAL_MACHINE</win:hive>
                        <win:key datatype="string">Software\\Microsoft\\Windows</win:key>
                        <win:name datatype="string">DisplayName</win:name>
                    </win:registry_object>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertEquals(1, objects.size());

        ParsedOvalObject object = objects.getFirst();
        assertEquals("oval:obj:1", object.getObjectId());
        assertEquals("registry_object", object.getObjectType());
        assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5#windows", object.getNamespace());

        assertEquals(3, object.getEntities().size());

        assertEntity(object.getEntities().get(0), "hive", "HKEY_LOCAL_MACHINE");
        assertEntity(object.getEntities().get(1), "key", "Software\\Microsoft\\Windows");
        assertEntity(object.getEntities().get(2), "name", "DisplayName");

        assertEquals("string", object.getEntities().get(0).getAttributes().get("datatype"));
        assertEquals("string", object.getEntities().get(1).getAttributes().get("datatype"));
        assertEquals("string", object.getEntities().get(2).getAttributes().get("datatype"));
    }

    @Test
    void readObject_shouldParseMultipleObjectsPreservingOrder() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1">
                        <win:name>One</win:name>
                    </win:registry_object>
                    <win:wmi57_object id="oval:obj:2">
                        <win:namespace>root\\cimv2</win:namespace>
                    </win:wmi57_object>
                    <win:family_object id="oval:obj:3"/>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertEquals(3, objects.size());

        assertEquals("oval:obj:1", objects.get(0).getObjectId());
        assertEquals("registry_object", objects.get(0).getObjectType());

        assertEquals("oval:obj:2", objects.get(1).getObjectId());
        assertEquals("wmi57_object", objects.get(1).getObjectType());

        assertEquals("oval:obj:3", objects.get(2).getObjectId());
        assertEquals("family_object", objects.get(2).getObjectType());
        assertTrue(objects.get(2).getEntities().isEmpty());
    }

    @Test
    void readObject_shouldIgnoreElementsThatDoNotEndWithObject() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <metadata>
                        <title>Ignore me</title>
                    </metadata>
                    <win:registry_object id="oval:obj:1">
                        <win:name>One</win:name>
                    </win:registry_object>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertEquals(1, objects.size());
        assertEquals("oval:obj:1", objects.getFirst().getObjectId());
    }

    @Test
    void readObject_shouldHandleObjectWithNoEntities() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:family_object id="oval:obj:1"/>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertEquals(1, objects.size());

        ParsedOvalObject object = objects.getFirst();
        assertEquals("oval:obj:1", object.getObjectId());
        assertEquals("family_object", object.getObjectType());
        assertTrue(object.getEntities().isEmpty());
    }

    @Test
    void readObject_shouldParseEntityAttributes() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1">
                        <win:key datatype="string" operation="pattern match">Software\\\\.*</win:key>
                    </win:registry_object>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        ParsedOvalEntity entity = objects.getFirst().getEntities().getFirst();

        assertEquals("key", entity.getName());
        assertEquals("Software\\\\.*", entity.getValue());
        assertEquals("string", entity.getAttributes().get("datatype"));
        assertEquals("pattern match", entity.getAttributes().get("operation"));
    }

    @Test
    void readObject_shouldSkipNestedEntityStructure() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1">
                        <win:set set_operator="UNION">
                            <win:object_reference>oval:obj:2</win:object_reference>
                            <win:object_reference>oval:obj:3</win:object_reference>
                        </win:set>
                    </win:registry_object>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertEquals(1, objects.size());
        assertEquals(1, objects.getFirst().getEntities().size());

        ParsedOvalEntity setEntity = objects.getFirst().getEntities().getFirst();
        assertEquals("set", setEntity.getName());
        assertEquals("UNION", setEntity.getAttributes().get("set_operator"));
        assertNull(setEntity.getValue());
    }

    @Test
    void readObject_shouldReturnEmptyListWhenObjectsElementIsEmpty() throws Exception {
        String xml = """
                <objects/>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        List<ParsedOvalObject> objects = objectReader.readObject(reader);

        assertNotNull(objects);
        assertTrue(objects.isEmpty());
    }

    @Test
    void readObject_shouldThrowWhenObjectsElementDoesNotClose() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1">
                        <win:name>DisplayName</win:name>
                    </win:registry_object>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        assertThrows(XMLStreamException.class, () -> objectReader.readObject(reader));
    }

    @Test
    void readObject_shouldThrowWhenObjectElementDoesNotClose() throws Exception {
        String xml = """
                <objects xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_object id="oval:obj:1">
                        <win:name>DisplayName</win:name>
                </objects>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "objects");

        assertThrows(XMLStreamException.class, () -> objectReader.readObject(reader));
    }

    private XMLStreamReader2 moveToStart(String xml, String elementName) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && elementName.equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("No " + elementName + " element found in test XML");
    }

    private void assertEntity(ParsedOvalEntity entity, String name, String value) {
        assertEquals(name, entity.getName());
        assertEquals(value, entity.getValue());
    }
}