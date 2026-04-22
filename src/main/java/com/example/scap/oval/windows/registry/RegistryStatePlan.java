package com.example.scap.oval.windows.registry;

import com.example.scap.model.normalized.oval.OvalEntityConstraint;
import com.example.scap.oval.CompiledOvalStatePlan;

import java.util.Optional;

public class RegistryStatePlan extends CompiledOvalStatePlan {
    public Optional<OvalEntityConstraint> type() {
        return findEntity("type");
    }

    public Optional<OvalEntityConstraint> value() {
        return findEntity("value");
    }
}
