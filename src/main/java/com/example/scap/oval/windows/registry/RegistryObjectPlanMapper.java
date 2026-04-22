package com.example.scap.oval.windows.registry;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.oval.GenericOvalObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistryObjectPlanMapper {
    private final GenericOvalObjectMapper genericMapper;

    public RegistryObjectPlan map(final ParsedOvalObject object) {
        if (!"registry_object".equals(object.getObjectType())) {
            throw new IllegalArgumentException("Expected registry_object but got: " + object.getObjectType());
        }

        RegistryObjectPlan plan = genericMapper.map(object, new RegistryObjectPlan());

        validate(plan);
        return plan;
    }

    private void validate(final RegistryObjectPlan plan) {
        if (plan.hive().isEmpty()) {
            throw new IllegalArgumentException("registry_object missing hive: " + plan.getObjectId());
        }

        if (plan.key().isEmpty()) {
            throw new IllegalArgumentException("registry_object missing key: " + plan.getObjectId());
        }
    }
}
