package com.example.scap.parser.reader.oval;

import com.example.scap.model.parsed.oval.ParsedOvalTest;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OvalTestReader {

    public List<ParsedOvalTest> readTest(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final List<ParsedOvalTest> parsedOvalTests = new ArrayList<>();

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.endsWith("_test")) {
                    parsedOvalTests.add(readTest(reader, localName));
                }
            }else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("tests")) {
                return parsedOvalTests;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading tests");
    }

    private ParsedOvalTest readTest(final XMLStreamReader2 reader, final String name)
            throws XMLStreamException {
        final ParsedOvalTest parsedOvalTest = new ParsedOvalTest();
        final String id = reader.getAttributeValue(null, "id");
        final String check = reader.getAttributeValue(null, "check");
        final String checkExistence = reader.getAttributeValue(null, "check_existence");
        final String stateOperator = reader.getAttributeValue(null, "state_operator");

        parsedOvalTest.setId(id);
        parsedOvalTest.setCheck(check);
        parsedOvalTest.setCheckExistence(checkExistence == null ? "at_least_one_exists" : checkExistence);
        parsedOvalTest.setState_operator(stateOperator == null ? "AND" : stateOperator);
        parsedOvalTest.setTestType(name);

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.equals("object")) {
                    parsedOvalTest.setObjectRef(reader.getAttributeValue(null, "object_ref"));
                } else if (localName.equals("state")) {
                    parsedOvalTest.getStateRef().add(reader.getAttributeValue(null, "state_ref"));
                }

            } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(name)) {
                return parsedOvalTest;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading " + name);
    }
}
