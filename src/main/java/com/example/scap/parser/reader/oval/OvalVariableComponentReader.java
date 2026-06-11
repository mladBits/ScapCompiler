package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.variables.*;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Slf4j
@Component
public class OvalVariableComponentReader {
    public ParsedOvalVariableComponent readComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final String localName = reader.getLocalName();

        return switch (localName) {
            case "literal_component" -> readLiteralComponent(reader);
            case "object_component" -> readObjectComponent(reader);
            case "variable_component" -> readVariableComponent(reader);
            case "concat" -> readConcat(reader);
            case "regex_capture" -> readRegexCapture(reader);
            default -> readUnsupportedComponent(reader, localName);
        };
    }

    private ParsedLiteralComponent readLiteralComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedLiteralComponent component = new ParsedLiteralComponent();
        component.setValue(reader.getElementText().trim());
        return component;
    }

    private ParsedObjectComponent readObjectComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedObjectComponent component = new ParsedObjectComponent();
        component.setObjectRef(reader.getAttributeValue(null, "object_ref"));
        component.setItemField(reader.getAttributeValue(null, "item_field"));

        skipToEndOfElement(reader, "object_component");
        return component;
    }

    private ParsedVariableComponent readVariableComponent(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedVariableComponent component = new ParsedVariableComponent();
        component.setVarRef(reader.getAttributeValue(null, "var_ref"));

        skipToEndOfElement(reader, "variable_component");
        return component;
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
                component.setComponent(readComponent(reader));
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "regex_capture".equals(reader.getLocalName())) {
                return component;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading regex_capture");
    }

    /**
     * An OVAL variable function this compiler does not implement (split, substring,
     * arithmetic, ...). The whole subtree is skipped so parsing survives; the compile
     * stage marks the owning variable unresolved.
     */
    private ParsedUnsupportedComponent readUnsupportedComponent(
            final XMLStreamReader2 reader,
            final String localName) throws XMLStreamException {
        log.warn("Skipping unsupported variable component: {}", localName);

        final ParsedUnsupportedComponent component = new ParsedUnsupportedComponent();
        component.setFunctionName(localName);

        skipToEndOfElement(reader, localName);
        return component;
    }

    /**
     * Consumes events up to and including the END_ELEMENT matching the element
     * the reader is currently positioned on. Handles nested elements of the
     * same name via depth counting.
     */
    private void skipToEndOfElement(final XMLStreamReader2 reader, final String localName)
            throws XMLStreamException {
        int depth = 0;

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT
                    && localName.equals(reader.getLocalName())) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && localName.equals(reader.getLocalName())) {
                if (depth == 0) {
                    return;
                }
                depth--;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading " + localName);
    }
}