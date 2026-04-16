package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class RuleReader {
    private final CheckReader checkReader;

    public ParsedXccdfRule readRule(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedXccdfRule rule = new ParsedXccdfRule();
        rule.setRuleId(reader.getAttributeValue(null, "id"));

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                if ("title".equals(localName) && rule.getTitle() == null) {
                    rule.setTitle(reader.getElementText().trim());
                } else if ("check".equals(localName) || "complex-check".equals(localName)) {
                    rule.getCheckReferences().add(checkReader.readCheckNode(reader));
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "Rule".equals(reader.getLocalName())) {
                return rule;
            }
        }
        throw new XMLStreamException("Unexpected end of document while reading Rule");
    }
}
