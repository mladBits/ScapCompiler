package com.example.scap.parser.reader.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedXccdfValue;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class ValueReader {
    public ParsedXccdfValue readVariable(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedXccdfValue value = new ParsedXccdfValue();
        value.setId(reader.getAttributeValue(null, "id"));

        final String operator = reader.getAttributeValue(null, "operator");
        value.setOperator(operator == null ? "equals" : operator);

        final String type = reader.getAttributeValue(null, "type");
        value.setType(type == null ? "string" : type);

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if ("title".equals(localName) && value.getTitle() == null) {
                    value.setTitle(reader.getElementText().trim());
                } else if ("value".equals(localName)) {
                    value.setValue(reader.getElementText().trim());
                } else if ("default".equals(localName)) {
                    value.setDefaultValue(reader.getElementText().trim());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "Value".equals(reader.getLocalName())) {
                return value;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading Value");
    }
}
