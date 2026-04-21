package com.example.scap.parser;


import com.example.scap.model.parsed.oval.ParsedOval;
import com.example.scap.parser.reader.oval.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OvalParserImplTest {
    private static final String resourceName = "oval.xml";

    @Test
    void testParserOval()
            throws IOException {
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(in);

            final OvalEntityReader ovalEntityReader = new OvalEntityReader();
            final OvalReader ovalReader = new OvalReader(new OvalDefinitionsReader(
                    new OvalDefinitionReader(new OvalCriteriaReader())),
                    new OvalTestReader(),
                    new OvalObjectReader(ovalEntityReader),
                    new OvalStateReader(ovalEntityReader));
            final OvalParser ovalParser = new OvalParserImpl(ovalReader);
            final ParsedOval parsedOval = ovalParser.parse(in);
            assertEquals(451, parsedOval.getDefinitions().size());
            assertEquals(297, parsedOval.getTests().size());
            assertEquals(228, parsedOval.getObjects().size());
            assertEquals(192, parsedOval.getStates().size());
        }
    }

}