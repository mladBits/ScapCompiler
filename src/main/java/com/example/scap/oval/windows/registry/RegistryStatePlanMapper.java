package com.example.scap.oval.windows.registry;

import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.oval.GenericOvalStateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistryStatePlanMapper {
    private final GenericOvalStateMapper genericMapper;

    public RegistryStatePlan map(final ParsedOvalState state) {
        if (!"registry_state".equals(state.getStateType())) {
            throw new IllegalArgumentException("Expected registry_state but got: " + state.getStateType());
        }

        return genericMapper.map(state, new RegistryStatePlan());
    }
}
