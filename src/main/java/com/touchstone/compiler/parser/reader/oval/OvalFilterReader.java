package com.touchstone.compiler.parser.reader.oval;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalFilter;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class OvalFilterReader {

    public ParsedOvalFilter readFilter(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalFilter filter = new ParsedOvalFilter();
        final String action = reader.getAttributeValue(null, "action");
        filter.setAction(action == null ? "exclude" : action);

        final StringBuilder text = new StringBuilder();

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                text.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "filter".equals(reader.getLocalName())) {
                final String stateRef = text.toString().trim();
                filter.setStateRef(stateRef.isBlank() ? null : stateRef);
                return filter;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading filter");
    }
}
