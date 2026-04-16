package com.example.scap.parser;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.parser.reader.xccdf.BenchmarkReader;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class XccdfParserImpl implements XccdfParser {
    private static final XMLInputFactory2 factory = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private final BenchmarkReader benchmarkReader;

    @Override
    public ParsedXccdfBenchmark parse(final InputStream inputStream) {
        try (inputStream) {
            final XMLStreamReader2 reader = (XMLStreamReader2) factory.createXMLStreamReader(inputStream);
            moveToBenchmarkStart(reader);
            return benchmarkReader.readBenchmark(reader);
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException("Failed to parse XCCDF benchmark", e);
        }
    }

    private void moveToBenchmarkStart(final XMLStreamReader reader)
            throws XMLStreamException {
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && "Benchmark".equals(reader.getLocalName())) {
                return;
            }
        }
        throw new XMLStreamException("No Benchmark root element found");
    }
}