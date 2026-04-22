package com.example.scap.parser.reader.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedCheckExport;
import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedComplexCheck;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class CheckReader {
    public ParsedCheckNode readCheckNode(final XMLStreamReader2 reader) throws XMLStreamException {
        final String localName = reader.getLocalName();
        return switch (localName) {
            case "check" -> readCheckLeaf(reader);
            case "complex-check" -> readComplexCheck(reader);
            default -> throw new XMLStreamException("Unsupported check node: " + localName);
        };
    }

    private ParsedCheckNode readCheckLeaf(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedCheckReference parsedCheckReference = new ParsedCheckReference();
        parsedCheckReference.setSystem(reader.getAttributeValue(null, "system"));
        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if ("check-content-ref".equals(localName)) {
                    parsedCheckReference.setHref(reader.getAttributeValue(null, "href"));
                    parsedCheckReference.setName(reader.getAttributeValue(null, "name"));
                } else if ("check-export".equals(localName)) {
                    final ParsedCheckExport checkExport = new ParsedCheckExport();
                    checkExport.setExportName(reader.getAttributeValue(null, "export-name"));
                    checkExport.setValueId(reader.getAttributeValue(null, "value-id"));
                    parsedCheckReference.getCheckExports().add(checkExport);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "check".equals(reader.getLocalName())) {
                return parsedCheckReference;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading check");
    }

    private ParsedCheckNode readComplexCheck(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedComplexCheck parsedComplexCheck = new ParsedComplexCheck();
        final String operator = reader.getAttributeValue(null, "operator");
        parsedComplexCheck.setOperator(operator == null ? "AND" : operator);

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if ("check".equals(localName)) {
                    parsedComplexCheck.getChildren().add(readCheckLeaf(reader));
                } else if ("complex-check".equals(localName)) {
                    parsedComplexCheck.getChildren().add(readComplexCheck(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "complex-check".equals(reader.getLocalName())) {
                return parsedComplexCheck;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading complex-check");
    }
}
