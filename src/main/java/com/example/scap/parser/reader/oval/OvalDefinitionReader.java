package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

public class OvalDefinitionReader {
    public ParsedOvalDefinition readDefinition(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalDefinition definition = new ParsedOvalDefinition();
        definition.setId(reader.getAttributeValue(null, "id"));
        definition.setClazz(reader.getAttributeValue(null, "class"));

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

            } else if (event == XMLStreamConstants.END_ELEMENT && "definition".equals(reader.getLocalName())) {
                return definition;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading definition");
    }
}
