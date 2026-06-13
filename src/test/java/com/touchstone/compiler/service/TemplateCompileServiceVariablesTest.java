package com.touchstone.compiler.service;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.touchstone.compiler.content.ContentPackage;
import com.touchstone.compiler.content.ContentPackageLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-pipeline test over the DISA STIG fixtures: variables must be
 * materialized into the template so the agent can resolve them at runtime.
 */
@SpringBootTest(properties = "app.compile-job-poller.enabled=false")
class TemplateCompileServiceVariablesTest {

    @TestConfiguration
    static class FixtureContentConfig {
        @Bean
        @Primary
        ContentPackageLoader fixtureContentPackageLoader() {
            return packageId -> new ContentPackage(
                    requireNonNull(getClass().getClassLoader().getResourceAsStream("xccdf.xml"),
                            "fixture xccdf.xml not on test classpath"),
                    requireNonNull(getClass().getClassLoader().getResourceAsStream("oval.xml"),
                            "fixture oval.xml not on test classpath"));
        }
    }

    @Autowired
    private TemplateCompileService templateCompileService;

    @Test
    void compile_withoutProfileIds_shouldCompileEveryProfileInThePackage() throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setPackageId("disa-windows-11-stig");

        var templates = templateCompileService.compileTemplates(request);

        // The DISA fixture defines 11 profiles (9 MAC levels + 2 extras).
        assertEquals(11, templates.size());
        assertEquals(11, templates.stream().map(t -> t.getProfileId()).distinct().count());
    }

    @Test
    void compile_withProfileSubset_shouldCompileOnlyThoseProfiles() throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setPackageId("disa-windows-11-stig");
        request.setProfileIds(List.of(
                "xccdf_mil.disa.stig_profile_MAC-1_Sensitive",
                "xccdf_mil.disa.stig_profile_MAC-3_Public"));

        var templates = templateCompileService.compileTemplates(request);

        assertEquals(
                List.of("xccdf_mil.disa.stig_profile_MAC-1_Sensitive",
                        "xccdf_mil.disa.stig_profile_MAC-3_Public"),
                templates.stream().map(t -> t.getProfileId()).toList());
    }

    @Test
    void compile_shouldMaterializeVariablesIntoTemplate() throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setPackageId("disa-windows-11-stig");
        request.setProfileIds(List.of("xccdf_mil.disa.stig_profile_MAC-1_Sensitive"));

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode template = objectMapper.valueToTree(templateCompileService.compileTemplates(request).getFirst());

        JsonNode variables = template.get("variablesById");
        assertTrue(variables != null && variables.size() > 0, "variablesById must be populated");

        // The fixture defines 9 local variables.
        JsonNode pathVariable = variables.get("oval:mil.disa.stig.win:var:25334001");
        assertEquals("local", pathVariable.get("kind").asText());
        assertEquals("string", pathVariable.get("datatype").asText());
        assertEquals("concat", pathVariable.get("expression").get("node").asText());

        // Objects referenced only by variable object components need collection plans.
        assertTrue(template.get("objectsById").has("oval:mil.disa.stig.win:obj:20000015"),
                "variable-referenced object must have a collection plan");

        // var_check survives into state assertions.
        boolean varCheckPresent = false;
        for (JsonNode state : template.get("statesById")) {
            for (JsonNode assertion : state.get("assertions")) {
                if (assertion.has("varCheck")) {
                    assertEquals("variable", assertion.get("expression").get("type").asText());
                    varCheckPresent = true;
                }
            }
        }
        assertTrue(varCheckPresent, "varCheck must be preserved on variable assertions");

        // No silent failures: every referenced variable resolved (fixture has no
        // external variables and all functions are supported).
        for (JsonNode variable : variables) {
            assertFalse(variable.path("unresolved").asBoolean(false),
                    "unexpected unresolved variable: " + variable.get("variableId").asText());
        }
    }
}
