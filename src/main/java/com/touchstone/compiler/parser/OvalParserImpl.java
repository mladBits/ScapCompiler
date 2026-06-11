package com.touchstone.compiler.parser;

import com.touchstone.compiler.model.parsed.oval.ParsedOval;
import com.touchstone.compiler.parser.reader.oval.OvalReader;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class OvalParserImpl implements OvalParser {
    private static final XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final OvalReader ovalReader;

    @Override
    public ParsedOval parse(final InputStream inputStream) {
        try (inputStream) {
            final XMLStreamReader2 reader = (XMLStreamReader2) factory.createXMLStreamReader(inputStream);
            return ovalReader.readOval(reader);
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException("Failed to parse OVAL", e);
        }
    }
}
