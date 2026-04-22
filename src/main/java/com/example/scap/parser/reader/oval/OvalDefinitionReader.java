package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class OvalDefinitionReader {
    private final OvalCriteriaReader criteriaReader;

    public ParsedOvalDefinition readDefinition(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalDefinition definition = new ParsedOvalDefinition();
        definition.setId(reader.getAttributeValue(null, "id"));
        definition.setDefClass(reader.getAttributeValue(null, "class"));

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if ("criteria".equals(localName)) {
                    definition.setCriteria(criteriaReader.readCriteria(reader));
                }

            } else if (event == XMLStreamConstants.END_ELEMENT && "definition".equals(reader.getLocalName())) {
                return definition;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading definition");
    }
}
