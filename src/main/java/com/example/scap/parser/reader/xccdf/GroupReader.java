package com.example.scap.parser.reader.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedXccdfGroup;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class GroupReader {
    private final RuleReader ruleReader;

    public ParsedXccdfGroup readGroup(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedXccdfGroup group = new ParsedXccdfGroup();
        group.setGroupId(reader.getAttributeValue(null, "id"));

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if ("title".equals(localName) && group.getTitle() == null) {
                    group.setTitle(reader.getElementText().trim());
                } else if ("Rule".equals(localName)) {
                    group.getRules().add(ruleReader.readRule(reader));
                } else if ("Group".equals(localName)) {
                    group.getGroups().add(readGroup(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "Group".equals(reader.getLocalName())) {
                return group;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading Group");
    }
}
