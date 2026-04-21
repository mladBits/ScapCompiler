package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OvalStateReader {
    private final OvalEntityReader entityReader;

    public List<ParsedOvalState> readState(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final List<ParsedOvalState> parsedOvalStates = new ArrayList<>();

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.endsWith("_state")) {
                    parsedOvalStates.add(readState(reader, localName));
                }
            }else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("states")) {
                return parsedOvalStates;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading states");
    }

    public ParsedOvalState readState(final XMLStreamReader2 reader, final String name)
            throws XMLStreamException {
        final ParsedOvalState state = new ParsedOvalState();

        state.setStateId(reader.getAttributeValue(null, "id"));
        state.setStateType(reader.getLocalName());
        state.setNamespace(reader.getNamespaceURI());

        final String stateElementName = reader.getLocalName();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final ParsedOvalEntity entity = entityReader.readEntity(reader);
                state.getEntities().add(entity);
            } else if (event == XMLStreamConstants.END_ELEMENT && stateElementName.equals(reader.getLocalName())) {
                return state;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading OVAL state: " + state.getStateId());
    }
}
