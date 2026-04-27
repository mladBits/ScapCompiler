package com.example.scap.model.parsed.oval;

import com.example.scap.oval.EntitySelector;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ParsedOvalEntity {
    private String name;
    private String value;
    private final Map<String, String> attributes = new HashMap<>();

    public EntitySelector resolve() {
        final EntitySelector entitySelector = new EntitySelector();
        entitySelector.setField(name);
        entitySelector.setDatatype(attributes.getOrDefault("datatype", "string"));
        entitySelector.setOperation(attributes.getOrDefault("operator", "equals"));

        if (attributes.getOrDefault("nil", "false").equals("true")) {
            entitySelector.setValue("nil", null);
        } else if (attributes.containsKey("var_ref")) {
            entitySelector.setValue("variable", attributes.get("var_ref"));
        } else {
            entitySelector.setValue("literal", value);
        }
        return entitySelector;
    }
}
