package com.example.scap.parser;

import com.example.scap.model.parsed.xccdf.*;
import com.example.scap.parser.reader.xccdf.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XccdfParserImplTest {
    private static final String resourceName = "xccdf.xml";

    @Test
    void testParserXccdf()
            throws IOException {
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(in);

            final RuleReader ruleReader = new RuleReader(new CheckReader());
            final XccdfParser parser = new XccdfParserImpl(
                    new BenchmarkReader(
                            new ProfileReader(),
                            ruleReader,
                            new GroupReader(ruleReader),
                            new ValueReader()));

            final ParsedXccdfBenchmark parsedXccdfBenchmark = parser.parse(in);
            assertEquals("xccdf_mil.disa.stig_benchmark_Microsoft_Windows_11_STIG", parsedXccdfBenchmark.getBenchmarkId());
            assertEquals("Microsoft Windows 11 STIG SCAP Benchmark", parsedXccdfBenchmark.getTitle());
            assertEquals(11, parsedXccdfBenchmark.getProfiles().size());

            final ParsedXccdfProfile profile = parsedXccdfBenchmark.getProfiles().getFirst();
            assertEquals("xccdf_mil.disa.stig_profile_MAC-1_Classified", profile.getProfileId());
            assertEquals("I - Mission Critical Classified", profile.getTitle());
            assertEquals(223, profile.getSelectedIdRefs().size());

            assertEquals(0, parsedXccdfBenchmark.getRules().size());
            assertEquals(223, parsedXccdfBenchmark.getGroups().size());

            final ParsedXccdfGroup group = parsedXccdfBenchmark.getGroups().getFirst();
            assertEquals("xccdf_mil.disa.stig_group_V-253254", group.getGroupId());
            assertEquals("SRG-OS-000480-GPOS-00227", group.getTitle());

            final ParsedXccdfRule rule = group.getRules().getFirst();
            assertEquals("xccdf_mil.disa.stig_rule_SV-253254r991589_rule", rule.getRuleId());
            assertEquals("Domain-joined systems must use Windows 11 Enterprise Edition 64-bit version.", rule.getTitle());
            assertEquals(1, rule.getCheckReferences().size());
            assertEquals("U_MS_Windows_11_V2R8_STIG_SCAP_1-3_Benchmark-oval.xml", ((ParsedCheckReference)rule.getCheckReferences().getFirst()).getHref());
            assertEquals("http://oval.mitre.org/XMLSchema/oval-definitions-5", ((ParsedCheckReference)rule.getCheckReferences().getFirst()).getSystem());
            assertEquals("oval:mil.disa.stig.windows11:def:253254", ((ParsedCheckReference)rule.getCheckReferences().getFirst()).getName());
        }
    }
}