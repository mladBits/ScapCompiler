package com.example.scap.parser.reader.xccdf;

import com.example.scap.model.parsed.xccdf.*;
import lombok.RequiredArgsConstructor;
import org.codehaus.stax2.XMLStreamReader2;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

@Component
@RequiredArgsConstructor
public class BenchmarkReader {
    private final ProfileReader profileReader;
    private final RuleReader ruleReader;
    private final GroupReader groupReader;
    private final ValueReader valueReader;

    public ParsedXccdfBenchmark readBenchmark(final XMLStreamReader2 reader)
            throws XMLStreamException {
        final ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();
        benchmark.setBenchmarkId(reader.getAttributeValue(null, "id"));

        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();

                switch (localName) {
                    case "title" -> {
                        if (benchmark.getTitle() == null) {
                            benchmark.setTitle(reader.getElementText().trim());
                        }
                    }
                    case "Profile" -> {
                        final ParsedXccdfProfile profile = profileReader.readProfile(reader);
                        benchmark.getProfiles().add(profile);
                    }
                    case "Value" -> {
                        final ParsedXccdfValue value = valueReader.readVariable(reader);
                        benchmark.getValues().add(value);
                    }
                    case "Rule" -> {
                        final ParsedXccdfRule rule = ruleReader.readRule(reader);
                        benchmark.getRules().add(rule);
                    }
                    case "Group" -> {
                        final ParsedXccdfGroup group = groupReader.readGroup(reader);
                        benchmark.getGroups().add(group);
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("Benchmark")) {
                return benchmark;
            }
        }

        throw new XMLStreamException("Unexpected end of document while reading Benchmark");
    }
}
