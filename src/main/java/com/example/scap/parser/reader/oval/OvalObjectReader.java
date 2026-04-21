package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OvalObjectReader {
    private final OvalEntityReader entityReader;

    public List<ParsedOvalObject> readObject(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final List<ParsedOvalObject> parsedOvalObjects = new ArrayList<>();

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.endsWith("_object")) {
                    parsedOvalObjects.add(readObject(reader, localName));
                }
            }else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("objects")) {
                return parsedOvalObjects;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading objects");
    }

    private ParsedOvalObject readObject(final XMLStreamReader2 reader, final String name)
            throws XMLStreamException {
        final ParsedOvalObject object = new ParsedOvalObject();

        object.setObjectId(reader.getAttributeValue(null, "id"));
        object.setObjectType(reader.getLocalName());
        object.setNamespace(reader.getNamespaceURI());

        final String objectElementName = reader.getLocalName();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final ParsedOvalEntity entity = entityReader.readEntity(reader);
                object.getEntities().add(entity);
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && objectElementName.equals(reader.getLocalName())) {
                return object;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading OVAL object: " + object.getObjectId());
    }
}
