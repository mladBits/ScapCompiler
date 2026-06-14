package com.touchstone.compiler.service;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.content.ContentPackage;
import com.touchstone.compiler.content.ContentPackageLoader;
import com.touchstone.compiler.model.compiled.ExecutionTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden-file contract test: the compiled output of each fixture is compared
 * (as JSON trees) against a checked-in expectation. The ExecutionTemplate is a
 * cross-language contract with the Go agent — any unintended change to its
 * shape must fail CI here. Two fixtures are covered so different content shapes
 * are exercised: DISA (local variables, regex_capture) and CIS Intune (heavy
 * external variables, concat/object_component chains).
 *
 * Intentional contract changes: regenerate with
 *   mvn test -Dtest=TemplateCompileServiceGoldenTest -Dgolden.update=true
 * review the golden diff, and bump ExecutionTemplate.CURRENT_SCHEMA_VERSION
 * if the change is breaking.
 */
@SpringBootTest(properties = "app.compile-job-poller.enabled=false")
class TemplateCompileServiceGoldenTest {

    /** A fixture content package: which XML to load and which golden it produces. */
    record Fixture(String packageId, String xccdf, String oval, String profileId, String golden) {
    }

    private static final List<Fixture> FIXTURES = List.of(
            new Fixture("disa-windows-11-stig", "xccdf.xml", "oval.xml",
                    "xccdf_mil.disa.stig_profile_MAC-1_Sensitive", "disa-mac1-sensitive.json"),
            new Fixture("cis-intune-win11", "cis-intune-win11-xccdf.xml", "cis-intune-win11-oval.xml",
                    "xccdf_org.cisecurity.benchmarks_profile_Level_1_L1", "cis-intune-win11-l1.json")
    );

    static Stream<Fixture> fixtures() {
        return FIXTURES.stream();
    }

    @TestConfiguration
    static class FixtureContentConfig {
        @Bean
        @Primary
        ContentPackageLoader fixtureContentPackageLoader() {
            return packageId -> FIXTURES.stream()
                    .filter(f -> f.packageId().equals(packageId))
                    .findFirst()
                    .map(f -> new ContentPackage(resource(f.xccdf()), resource(f.oval())))
                    .orElseThrow(() -> new IllegalArgumentException("no fixture for " + packageId));
        }

        private static InputStream resource(String name) {
            return requireNonNull(FixtureContentConfig.class.getClassLoader().getResourceAsStream(name),
                    "missing test resource: " + name);
        }
    }

    @Autowired
    private TemplateCompileService templateCompileService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    void compiledTemplate_shouldMatchGoldenFile(Fixture fixture) throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setPackageId(fixture.packageId());
        request.setProfileIds(List.of(fixture.profileId()));

        ExecutionTemplate template = templateCompileService.compileTemplates(request).getFirst();
        // Neutralize the two volatile fields; everything else must be stable.
        template.setTemplateId("00000000-0000-0000-0000-000000000000");
        template.setGeneratedAt(Instant.EPOCH);

        // Mirrors the JacksonConfig contract mapper (ISO-8601 dates).
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JsonNode actual = mapper.valueToTree(template);

        Path goldenPath = Path.of("src/test/resources/golden", fixture.golden());
        if (Boolean.getBoolean("golden.update")) {
            Files.createDirectories(goldenPath.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(goldenPath.toFile(), actual);
            return;
        }

        JsonNode expected;
        try (InputStream golden = getClass().getClassLoader()
                .getResourceAsStream("golden/" + fixture.golden())) {
            expected = mapper.readTree(requireNonNull(golden,
                    "Golden file missing; generate it with -Dgolden.update=true"));
        }

        if (!expected.equals(actual)) {
            Path actualDump = Path.of("target/golden-actual-" + fixture.golden());
            mapper.writerWithDefaultPrettyPrinter().writeValue(actualDump.toFile(), actual);
            fail("Compiled template no longer matches " + fixture.golden() + " — the agent contract drifted. "
                    + "Actual output written to " + actualDump + ". If the change is intentional, regenerate "
                    + "with -Dgolden.update=true and bump ExecutionTemplate.CURRENT_SCHEMA_VERSION if breaking.");
        }
    }
}
