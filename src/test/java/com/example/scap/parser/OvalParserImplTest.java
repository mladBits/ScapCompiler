package com.example.scap.parser;


import com.example.scap.model.parsed.oval.ParsedOval;
import com.example.scap.parser.reader.oval.OvalDefinitionReader;
import com.example.scap.parser.reader.oval.OvalDefinitionsReader;
import com.example.scap.parser.reader.oval.OvalReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class OvalParserImplTest {
    private static final String resourceName = "oval.xml";

    @Test
    void testParserOval()
            throws IOException {
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(in);

            final OvalReader ovalReader = new OvalReader(new OvalDefinitionsReader(new OvalDefinitionReader()));
            final OvalParser ovalParser = new OvalParserImpl(ovalReader);
            ParsedOval parsedOval = ovalParser.parse(in);
            int x = 1;
        }
    }

}