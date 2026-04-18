package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOval;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class OvalReader {
    private final OvalDefinitionsReader ovalDefinitionsReader;
    private final OvalTestReader ovalTestReader;

    public ParsedOval readOval(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOval parsedOval = new ParsedOval();
        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                switch (localName) {
                    case "definitions" -> parsedOval.setDefinitions(ovalDefinitionsReader.readDefinitions(reader));
                    case "tests" -> parsedOval.setTests(ovalTestReader.readTest(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("oval_definitions")) {
                return parsedOval;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading Oval");

    }
}
