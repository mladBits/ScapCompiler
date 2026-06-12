package com.touchstone.compiler.service;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.content.ContentPackage;
import com.touchstone.compiler.content.ContentPackageLoader;
import com.touchstone.compiler.model.compiled.ExecutionTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden-file contract test: the compiled output of the DISA STIG fixture is
 * compared byte-for-byte (as JSON trees) against a checked-in expectation.
 * The ExecutionTemplate is a cross-language contract with the Go agent — any
 * unintended change to its shape must fail CI here.
 *
 * Intentional contract changes: regenerate with
 *   mvn test -Dtest=TemplateCompileServiceGoldenTest -Dgolden.update=true
 * review the golden diff, and bump ExecutionTemplate.CURRENT_SCHEMA_VERSION
 * if the change is breaking.
 */
@SpringBootTest(properties = "app.compile-job-poller.enabled=false")
class TemplateCompileServiceGoldenTest {

    private static final Path GOLDEN_PATH = Path.of("src/test/resources/golden/disa-mac1-sensitive.json");

    @TestConfiguration
    static class FixtureContentConfig {
        @Bean
        @Primary
        ContentPackageLoader fixtureContentPackageLoader() {
            return packageId -> new ContentPackage(
                    requireNonNull(getClass().getClassLoader().getResourceAsStream("xccdf.xml")),
                    requireNonNull(getClass().getClassLoader().getResourceAsStream("oval.xml")));
        }
    }

    @Autowired
    private TemplateCompileService templateCompileService;

    @Test
    void compiledTemplate_shouldMatchGoldenFile() throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setPackageId("disa-windows-11-stig");
        request.setProfileIds(List.of("xccdf_mil.disa.stig_profile_MAC-1_Sensitive"));

        ExecutionTemplate template = templateCompileService.compileTemplates(request).getFirst();
        // Neutralize the two volatile fields; everything else must be stable.
        template.setTemplateId("00000000-0000-0000-0000-000000000000");
        template.setGeneratedAt(Instant.EPOCH);

        // Mirrors the JacksonConfig contract mapper (ISO-8601 dates).
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonNode actual = mapper.valueToTree(template);

        if (Boolean.getBoolean("golden.update")) {
            Files.createDirectories(GOLDEN_PATH.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(GOLDEN_PATH.toFile(), actual);
            return;
        }

        JsonNode expected;
        try (InputStream golden = getClass().getClassLoader()
                .getResourceAsStream("golden/disa-mac1-sensitive.json")) {
            expected = mapper.readTree(requireNonNull(golden,
                    "Golden file missing; generate it with -Dgolden.update=true"));
        }

        if (!expected.equals(actual)) {
            Path actualDump = Path.of("target/golden-actual.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(actualDump.toFile(), actual);
            fail("Compiled template no longer matches the golden file — the agent contract drifted. "
                    + "Actual output written to " + actualDump + " for diffing against " + GOLDEN_PATH
                    + ". If the change is intentional, regenerate with -Dgolden.update=true and "
                    + "bump ExecutionTemplate.CURRENT_SCHEMA_VERSION if it is breaking.");
        }
    }
}
