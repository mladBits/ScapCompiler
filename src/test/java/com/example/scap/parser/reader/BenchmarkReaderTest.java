package com.example.scap.parser.reader;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfProfile;
import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import com.example.scap.parser.reader.xccdf.*;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkReaderTest {
    private static final XMLInputFactory2 FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
    private static final RuleReader ruleReader = new RuleReader(new CheckReader());
    private static final BenchmarkReader benchmarkReader = new BenchmarkReader(
            new ProfileReader(), ruleReader, new GroupReader(ruleReader), new ValueReader());

    @Test
    void readBenchmark_shouldParseIdTitleProfilesAndRules() throws Exception {
        String xml = """
                <Benchmark id="benchmark-1">
                    <title>  Sample Benchmark  </title>

                    <Profile id="profile-1">
                        <title>Profile One</title>
                        <select idref="rule-1" selected="true"/>
                        <select idref="rule-2" selected="false"/>
                        <select idref="rule-3" selected="true"/>
                    </Profile>

                    <Profile id="profile-2">
                        <title>Profile Two</title>
                        <select idref="rule-4" selected="true"/>
                    </Profile>

                    <Rule id="rule-1">
                        <title>Rule One</title>
                    </Rule>

                    <Rule id="rule-2">
                        <title>Rule Two</title>
                    </Rule>
                </Benchmark>
                """;

        XMLStreamReader2 reader = moveToBenchmarkStart(xml);

        ParsedXccdfBenchmark benchmark = benchmarkReader.readBenchmark(reader);

        assertEquals("benchmark-1", benchmark.getBenchmarkId());
        assertEquals("Sample Benchmark", benchmark.getTitle());

        assertEquals(2, benchmark.getProfiles().size());
        ParsedXccdfProfile firstProfile = benchmark.getProfiles().getFirst();
        assertEquals("profile-1", firstProfile.getProfileId());
        assertEquals("Profile One", firstProfile.getTitle());
        assertEquals(List.of("rule-1", "rule-3"), firstProfile.getSelectedIdRefs());

        assertEquals(2, benchmark.getRules().size());
        ParsedXccdfRule firstRule = benchmark.getRules().getFirst();
        assertEquals("rule-1", firstRule.getRuleId());
        assertEquals("Rule One", firstRule.getTitle());
    }

    @Test
    void readBenchmark_shouldUseFirstTitleWhenMultipleTitlesExist() throws Exception {
        String xml = """
                <Benchmark id="benchmark-2">
                    <title>First Benchmark Title</title>
                    <title>Second Benchmark Title</title>
                </Benchmark>
                """;

        XMLStreamReader2 reader = moveToBenchmarkStart(xml);

        ParsedXccdfBenchmark benchmark = benchmarkReader.readBenchmark(reader);

        assertEquals("benchmark-2", benchmark.getBenchmarkId());
        assertEquals("First Benchmark Title", benchmark.getTitle());
    }

    @Test
    void readBenchmark_shouldIgnoreUnrelatedElements() throws Exception {
        String xml = """
                <Benchmark id="benchmark-3">
                    <description>Ignore this</description>
                    <notice id="notice-1"/>
                    <version>1.0</version>

                    <Profile id="profile-1">
                        <title>Profile One</title>
                    </Profile>

                    <Rule id="rule-1">
                        <title>Rule One</title>
                    </Rule>
                </Benchmark>
                """;

        XMLStreamReader2 reader = moveToBenchmarkStart(xml);

        ParsedXccdfBenchmark benchmark = benchmarkReader.readBenchmark(reader);

        assertEquals("benchmark-3", benchmark.getBenchmarkId());
        assertNull(benchmark.getTitle());
        assertEquals(1, benchmark.getProfiles().size());
        assertEquals(1, benchmark.getRules().size());
    }

    @Test
    void readBenchmark_shouldHandleNoProfilesOrRules() throws Exception {
        String xml = """
                <Benchmark id="benchmark-4">
                    <title>Empty Benchmark</title>
                </Benchmark>
                """;

        XMLStreamReader2 reader = moveToBenchmarkStart(xml);

        ParsedXccdfBenchmark benchmark = benchmarkReader.readBenchmark(reader);

        assertEquals("benchmark-4", benchmark.getBenchmarkId());
        assertEquals("Empty Benchmark", benchmark.getTitle());
        assertTrue(benchmark.getProfiles().isEmpty());
        assertTrue(benchmark.getRules().isEmpty());
    }

    @Test
    void readBenchmark_shouldThrowWhenDocumentEndsBeforeBenchmarkCloses() throws Exception {
        String xml = """
                <Benchmark id="benchmark-5">
                    <title>Broken Benchmark</title>
                    <Profile id="profile-1">
                        <title>Profile One</title>
                    </Profile>
                """;

        XMLStreamReader2 reader = moveToBenchmarkStart(xml);

        XMLStreamException ex = assertThrows(
                XMLStreamException.class,
                () -> benchmarkReader.readBenchmark(reader)
        );

        assertNotNull(ex.getMessage());
    }

    private XMLStreamReader2 moveToBenchmarkStart(String xml) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLStreamReader2 reader = (XMLStreamReader2) FACTORY.createXMLStreamReader(in);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT
                    && "Benchmark".equals(reader.getLocalName())) {
                return reader;
            }
        }

        throw new IllegalStateException("No Benchmark element found in test XML");
    }
}