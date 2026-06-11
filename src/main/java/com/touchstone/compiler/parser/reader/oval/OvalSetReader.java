package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalSet;
import lombok.AllArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@AllArgsConstructor
public class OvalSetReader {
    private OvalFilterReader filterReader;

    public ParsedOvalSet readSet(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalSet set = new ParsedOvalSet();
        final String operator = reader.getAttributeValue(null, "operator");
        set.setOperator(operator == null ? "UNION" : operator);

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if ("object_reference".equals(localName)) {
                    final String objectRef = reader.getElementText().trim();
                    if (!objectRef.isBlank()) {
                        set.getObjectRefs().add(objectRef);
                    }
                } else if ("filter".equals(localName)) {
                    set.getFilters().add(filterReader.readFilter(reader));
                } else if ("set".equals(localName)) {
                    set.getChildSets().add(readSet(reader));
                } else {
                    skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "set".equals(reader.getLocalName())) {
                return set;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading set");
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