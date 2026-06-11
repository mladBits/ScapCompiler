package com.touchstone.compiler.model.parsed.oval;

import com.touchstone.compiler.oval.EntitySelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParsedOvalEntityTest {

    @Test
    void resolve_shouldPreserveVarCheckWithVariableReference() {
        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName("value");
        entity.getAttributes().put("var_ref", "oval:t:var:1");
        entity.getAttributes().put("var_check", "at least one");

        EntitySelector selector = entity.resolve();

        assertEquals("at least one", selector.getVarCheck());
    }

    @Test
    void resolve_shouldDefaultVarCheckToAllWhenVariableReferencePresent() {
        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName("value");
        entity.getAttributes().put("var_ref", "oval:t:var:1");

        EntitySelector selector = entity.resolve();

        assertEquals("all", selector.getVarCheck());
    }

    @Test
    void resolve_shouldLeaveVarCheckNullForLiteralValues() {
        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName("value");
        entity.setValue("42");

        EntitySelector selector = entity.resolve();

        assertNull(selector.getVarCheck());
    }
}