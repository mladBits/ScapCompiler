package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedXccdfProfile;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
public class ProfileReader {
    public ParsedXccdfProfile readProfile(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedXccdfProfile profile = new ParsedXccdfProfile();
        profile.setProfileId(reader.getAttributeValue(null, "id"));

        while (reader.hasNext()) {
            final int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                switch (localName) {
                    case "title" -> {
                        if (profile.getTitle() == null) {
                            profile.setTitle(reader.getElementText().trim());
                        }
                    }
                    case "select" -> {
                        final String idref = reader.getAttributeValue(null, "idref");
                        final String selected = reader.getAttributeValue(null, "selected");

                        if (idref != null && Boolean.parseBoolean(selected)) {
                            profile.getSelectedRuleIds().add(idref);
                        }
                    }
                    default -> {
                        // ignore other elements
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && "Profile".equals(reader.getLocalName())) {
                return profile;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading Profile");
    }
}
