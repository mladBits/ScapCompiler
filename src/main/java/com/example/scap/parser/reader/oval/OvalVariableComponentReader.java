package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.variables.*;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class OvalVariableComponentReader {
    public ParsedOvalVariableComponent readComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final String localName = reader.getLocalName();

        return switch (localName) {
            case "literal_component" -> readLiteralComponent(reader);
            case "object_component" -> readObjectComponent(reader);
            case "concat" -> readConcat(reader);
            case "regex_capture" -> readRegexCapture(reader);
            default -> throw new XMLStreamException("Unsupported variable component: " + localName);
        };
    }

    private ParsedLiteralComponent readLiteralComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedLiteralComponent component = new ParsedLiteralComponent();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT && "value".equals(reader.getLocalName())) {
                component.setValue(reader.getElementText().trim());
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "literal_component".equals(reader.getLocalName())) {
                return component;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading literal_component");
    }

    private ParsedObjectComponent readObjectComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedObjectComponent component = new ParsedObjectComponent();
        component.setObjectRef(reader.getAttributeValue(null, "object_ref"));
        component.setItemField(reader.getAttributeValue(null, "item_field"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.END_ELEMENT
                    && "object_component".equals(reader.getLocalName())) {
                return component;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading object_component");
    }

    private ParsedConcatComponent readConcat(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedConcatComponent component = new ParsedConcatComponent();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                component.getComponents().add(readComponent(reader));
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "concat".equals(reader.getLocalName())) {
                return component;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading concat");
    }

    private ParsedRegexCaptureComponent readRegexCapture(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedRegexCaptureComponent component = new ParsedRegexCaptureComponent();
        component.setPattern(reader.getAttributeValue(null, "pattern"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if ("object_component".contains(localName)) {
                    component.setComponent(readComponent(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "regex_capture".equals(reader.getLocalName())) {
                return component;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading regex_capture");
    }

}
