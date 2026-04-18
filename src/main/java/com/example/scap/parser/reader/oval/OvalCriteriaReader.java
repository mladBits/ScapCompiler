package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalCriteria;
import com.example.scap.model.parsed.oval.ParsedOvalCriterion;
import com.example.scap.model.parsed.oval.ParsedOvalExtendedDefinition;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class OvalCriteriaReader {

    public ParsedOvalCriteria readCriteria(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedOvalCriteria parsedOvalCriteria = new ParsedOvalCriteria();
        final String operator = reader.getAttributeValue(null, "operator");
        parsedOvalCriteria.setOperator(operator == null ? "AND" : operator);

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                switch (localName) {
                    case "criterion" -> {
                        final ParsedOvalCriterion criterion = new ParsedOvalCriterion();
                        criterion.setTestRef(reader.getAttributeValue(null, "test_ref"));
                        parsedOvalCriteria.getChildren().add(criterion);
                    }
                    case "criteria" -> parsedOvalCriteria.getChildren().add(readCriteria(reader));
                    case "extend_definition" -> {
                        final ParsedOvalExtendedDefinition extendedDefinition = new ParsedOvalExtendedDefinition();
                        extendedDefinition.setDefinitionRef(reader.getAttributeValue(null, "definition_ref"));
                        parsedOvalCriteria.getChildren().add(extendedDefinition);
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "criteria".equals(reader.getLocalName())) {
                return parsedOvalCriteria;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading Group");
    }
}
