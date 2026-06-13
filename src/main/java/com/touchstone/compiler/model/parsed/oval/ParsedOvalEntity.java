package com.touchstone.compiler.model.parsed.oval;

import com.touchstone.compiler.oval.EntitySelector;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ParsedOvalEntity {
    private String name;
    private String value;
    private final Map<String, String> attributes = new HashMap<>();

    /**
     * Record-datatype entities carry nested field assertions instead of a
     * scalar value (each field's name comes from its "name" attribute).
     */
    private final List<ParsedOvalEntity> fields = new ArrayList<>();

    public EntitySelector resolve() {
        final EntitySelector entitySelector = new EntitySelector();
        entitySelector.setField(name);
        entitySelector.setDatatype(attributes.getOrDefault("datatype", "string"));
        entitySelector.setOperation(attributes.getOrDefault("operation", "equals"));

        if (!fields.isEmpty()) {
            fields.forEach(field -> entitySelector.getFields().add(field.resolve()));
            return entitySelector;
        }

        if (attributes.getOrDefault("nil", "false").equals("true")) {
            entitySelector.setValue("nil", null);
        } else if (attributes.containsKey("var_ref")) {
            entitySelector.setValue("variable", attributes.get("var_ref"));
            entitySelector.setVarCheck(attributes.getOrDefault("var_check", "all"));
        } else {
            entitySelector.setValue("literal", value);
        }
        return entitySelector;
    }
}
