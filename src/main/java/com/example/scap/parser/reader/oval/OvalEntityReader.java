package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class OvalEntityReader {
    public ParsedOvalEntity readEntity(final XMLStreamReader2 reader)
        throws XMLStreamException {
        final ParsedOvalEntity entity =  new ParsedOvalEntity();
        final String elementName = reader.getLocalName();
        entity.setName(elementName);

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            entity.getAttributes().put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }

        final StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                text.append(reader.getText());
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                // First pass: skip nested structure inside entity-like nodes.
                // This handles <set>, <object_reference>, <filter>, etc. later if needed.
                skipElement(reader);
            } else if (event == XMLStreamConstants.END_ELEMENT && elementName.equals(reader.getLocalName())) {
                final String value = text.toString().trim();
                entity.setValue(value.isBlank() ? null : value);
                return entity;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading OVAL entity: " + elementName);
    }


    private void skipElement(final XMLStreamReader2 reader) throws XMLStreamException {
        final String startName = reader.getLocalName();
        int depth = 1;

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;

                if (depth == 0 && startName.equals(reader.getLocalName())) {
                    return;
                }
            }
        }

        throw new XMLStreamException("Unexpected end of document while skipping element: " + startName);
    }
}
