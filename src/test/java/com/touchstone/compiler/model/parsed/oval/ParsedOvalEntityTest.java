package com.touchstone.compiler.model.parsed.oval;

import com.touchstone.compiler.oval.EntitySelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void resolve_shouldEmitNestedFieldSelectorsForRecordEntities() {
        ParsedOvalEntity domainRole = new ParsedOvalEntity();
        domainRole.setName("domainrole");
        domainRole.setValue("0");
        domainRole.getAttributes().put("datatype", "int");

        ParsedOvalEntity record = new ParsedOvalEntity();
        record.setName("result");
        record.getAttributes().put("datatype", "record");
        record.getFields().add(domainRole);

        EntitySelector selector = record.resolve();

        // Record entities carry nested field selectors, not a scalar expression.
        assertNull(selector.getExpression());
        assertEquals(1, selector.getFields().size());

        EntitySelector field = selector.getFields().getFirst();
        assertEquals("domainrole", field.getField());
        assertEquals("int", field.getDatatype());
        assertTrue(String.valueOf((Object) field.getExpression()).contains("0"));
    }
}