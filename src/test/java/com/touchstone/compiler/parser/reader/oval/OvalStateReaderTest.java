package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalEntity;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
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

class OvalStateReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final OvalStateReader stateReader = new OvalStateReader(new OvalEntityReader());

    @Test
    void readState_shouldParseSingleStateWithEntities() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1" version="1">
                        <win:type datatype="string">reg_dword</win:type>
                        <win:value datatype="int" operation="equals">1</win:value>
                    </win:registry_state>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertEquals(1, states.size());

        ParsedOvalState state = states.getFirst();
        assertEquals("oval:ste:1", state.getStateId());
        assertEquals("registry_state", state.getStateType());
        assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5#windows", state.getNamespace());

        assertEquals(2, state.getEntities().size());

        assertEntity(state.getEntities().get(0), "type", "reg_dword");
        assertEntity(state.getEntities().get(1), "value", "1");

        assertEquals("string", state.getEntities().get(0).getAttributes().get("datatype"));
        assertEquals("int", state.getEntities().get(1).getAttributes().get("datatype"));
        assertEquals("equals", state.getEntities().get(1).getAttributes().get("operation"));
    }

    @Test
    void readState_shouldParseMultipleStatesPreservingOrder() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1">
                        <win:value>1</win:value>
                    </win:registry_state>
                    <win:wmi57_state id="oval:ste:2">
                        <win:value>Installed</win:value>
                    </win:wmi57_state>
                    <win:family_state id="oval:ste:3"/>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertEquals(3, states.size());

        assertEquals("oval:ste:1", states.get(0).getStateId());
        assertEquals("registry_state", states.get(0).getStateType());

        assertEquals("oval:ste:2", states.get(1).getStateId());
        assertEquals("wmi57_state", states.get(1).getStateType());

        assertEquals("oval:ste:3", states.get(2).getStateId());
        assertEquals("family_state", states.get(2).getStateType());
        assertTrue(states.get(2).getEntities().isEmpty());
    }

    @Test
    void readState_shouldIgnoreElementsThatDoNotEndWithState() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <metadata>
                        <title>Ignore me</title>
                    </metadata>
                    <win:registry_state id="oval:ste:1">
                        <win:value>1</win:value>
                    </win:registry_state>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertEquals(1, states.size());
        assertEquals("oval:ste:1", states.getFirst().getStateId());
    }

    @Test
    void readState_shouldHandleStateWithNoEntities() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:family_state id="oval:ste:1"/>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertEquals(1, states.size());

        ParsedOvalState state = states.getFirst();
        assertEquals("oval:ste:1", state.getStateId());
        assertEquals("family_state", state.getStateType());
        assertTrue(state.getEntities().isEmpty());
    }

    @Test
    void readState_shouldParseEntityAttributes() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1">
                        <win:value datatype="int" operation="greater than or equal">10</win:value>
                    </win:registry_state>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        ParsedOvalEntity entity = states.getFirst().getEntities().getFirst();

        assertEquals("value", entity.getName());
        assertEquals("10", entity.getValue());
        assertEquals("int", entity.getAttributes().get("datatype"));
        assertEquals("greater than or equal", entity.getAttributes().get("operation"));
    }

    @Test
    void readState_shouldSkipNestedEntityStructure() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1">
                        <win:set set_operator="UNION">
                            <win:state_reference>oval:ste:2</win:state_reference>
                            <win:state_reference>oval:ste:3</win:state_reference>
                        </win:set>
                    </win:registry_state>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertEquals(1, states.size());
        assertEquals(1, states.getFirst().getEntities().size());

        ParsedOvalEntity setEntity = states.getFirst().getEntities().getFirst();
        assertEquals("set", setEntity.getName());
        assertEquals("UNION", setEntity.getAttributes().get("set_operator"));
        assertNull(setEntity.getValue());
    }

    @Test
    void readState_shouldReturnEmptyListWhenStatesElementIsEmpty() throws Exception {
        String xml = """
                <states/>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        List<ParsedOvalState> states = stateReader.readState(reader);

        assertNotNull(states);
        assertTrue(states.isEmpty());
    }

    @Test
    void readState_shouldThrowWhenStatesElementDoesNotClose() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1">
                        <win:value>1</win:value>
                    </win:registry_state>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        assertThrows(XMLStreamException.class, () -> stateReader.readState(reader));
    }

    @Test
    void readState_shouldThrowWhenStateElementDoesNotClose() throws Exception {
        String xml = """
                <states xmlns:win="http://oval.mitre.org/XMLSchema/oval-definitions-5#windows">
                    <win:registry_state id="oval:ste:1">
                        <win:value>1</win:value>
                </states>
                """;

        XMLStreamReader2 reader = moveToStart(xml, "states");

        assertThrows(XMLStreamException.class, () -> stateReader.readState(reader));
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