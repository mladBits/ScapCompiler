package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalDefinitions;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class OvalDefinitionsReader {
    private final OvalDefinitionReader definitionReader;

    public ParsedOvalDefinitions readDefinitions(final XMLStreamReader2 reader)
            throws XMLStreamException {

        final ParsedOvalDefinitions parsedOvalDefinitions = new ParsedOvalDefinitions();

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if (localName.equals("definition")) {
                    parsedOvalDefinitions.getDefinitions().add(definitionReader.readDefinition(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("definitions")) {
                return parsedOvalDefinitions;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading definitions");
    }
}
