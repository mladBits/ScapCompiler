package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalEntity;
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
                if ("field".equals(reader.getLocalName())) {
                    // Record-datatype entities assert nested named fields
                    // (e.g. wmi57 result records).
                    entity.getFields().add(readField(reader));
                } else {
                    // Other nested structure (<set>, <filter>, ...) is handled
                    // by its dedicated reader; skip here.
                    skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && elementName.equals(reader.getLocalName())) {
                final String value = text.toString().trim();
                entity.setValue(value.isBlank() ? null : value);
                return entity;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading OVAL entity: " + elementName);
    }


    /**
     * Reads one OVAL record field. Unlike regular entities, a field's name is
     * its "name" attribute, not the element name.
     */
    private ParsedOvalEntity readField(final XMLStreamReader2 reader) throws XMLStreamException {
        final ParsedOvalEntity field = new ParsedOvalEntity();

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            field.getAttributes().put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        field.setName(field.getAttributes().get("name"));

        final StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                text.append(reader.getText());
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                skipElement(reader);
            } else if (event == XMLStreamConstants.END_ELEMENT && "field".equals(reader.getLocalName())) {
                final String value = text.toString().trim();
                field.setValue(value.isBlank() ? null : value);
                return field;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading record field");
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
