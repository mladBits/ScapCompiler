package com.touchstone.compiler.service;

import com.touchstone.compiler.api.dto.CompileTemplateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full-pipeline test over the DISA STIG fixtures: variables must be
 * materialized into the template so the agent can resolve them at runtime.
 * Uses the same hardcoded fixture paths as TemplateCompileService (to be
 * replaced by the content-package loader).
 */
@SpringBootTest
class TemplateCompileServiceVariablesTest {

    @Autowired
    private TemplateCompileService templateCompileService;

    @Test
    void compile_shouldMaterializeVariablesIntoTemplate() throws Exception {
        CompileTemplateRequest request = new CompileTemplateRequest();
        request.setBenchmarkId("benchmark");
        request.setProfileId("xccdf_mil.disa.stig_profile_MAC-1_Sensitive");

        templateCompileService.compile(request);

        JsonNode template = new ObjectMapper().readTree(Path.of("./test.json").toFile());

        JsonNode variables = template.get("variablesById");
        assertTrue(variables != null && variables.size() > 0, "variablesById must be populated");

        // The fixture defines 9 local variables; every referenced one is a PLAN.
        JsonNode pathVariable = variables.get("oval:mil.disa.stig.win:var:25334001");
        assertEquals("PLAN", pathVariable.get("kind").asText());
        assertEquals("string", pathVariable.get("datatype").asText());
        assertEquals("concat", pathVariable.get("expression").get("function").asText());

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
            assertFalse("UNRESOLVED".equals(variable.get("kind").asText()),
                    "unexpected UNRESOLVED variable: " + variable.get("variableId").asText());
        }
    }
}