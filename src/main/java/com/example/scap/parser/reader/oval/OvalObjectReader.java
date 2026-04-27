package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.*;
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
    private final OvalSetReader setReader;
    private final OvalFilterReader filterReader;

    public List<ParsedOvalObjectBase> readObject(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final List<ParsedOvalObjectBase> parsedOvalObjects = new ArrayList<>();

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

    private ParsedOvalObjectBase readObject(final XMLStreamReader2 reader, final String name)
            throws XMLStreamException {
        final String id = reader.getAttributeValue(null, "id");
        final String objectType = reader.getLocalName();
        final String namespace = reader.getNamespaceURI();
        final String objectElementName = reader.getLocalName();
        final List<ParsedOvalEntity> entities = new ArrayList<>();
        final List<ParsedOvalFilter> filters = new ArrayList<>();


        ParsedOvalSet parsedOvalSet = null;

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if ("set".equals(localName)) {
                    parsedOvalSet = setReader.readSet(reader);
                } else if ("filter".equals(localName)) {
                    filters.add(filterReader.readFilter(reader));
                } else {
                    entities.add(entityReader.readEntity(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && objectElementName.equals(reader.getLocalName())) {

                if (parsedOvalSet == null) {
                    final ParsedOvalObject parsedOvalObject = new ParsedOvalObject();
                    parsedOvalObject.setObjectId(id);
                    parsedOvalObject.setObjectType(objectType);
                    parsedOvalObject.setNamespace(namespace);
                    parsedOvalObject.getEntities().addAll(entities);
                    parsedOvalObject.getFilters().addAll(filters);
                    return parsedOvalObject;
                }

                final ParsedOvalObjectSet parsedOvalObjectSet = new ParsedOvalObjectSet();
                parsedOvalObjectSet.setObjectId(id);
                parsedOvalObjectSet.setObjectType(objectType);
                parsedOvalObjectSet.setNamespace(namespace);
                parsedOvalObjectSet.setSet(parsedOvalSet);
                parsedOvalObjectSet.getFilters().addAll(filters);
                return parsedOvalObjectSet;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading OVAL object: " + id);
    }
}
