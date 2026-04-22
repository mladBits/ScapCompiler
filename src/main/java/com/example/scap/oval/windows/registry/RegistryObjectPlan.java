package com.example.scap.oval.windows.registry;

import com.example.scap.model.normalized.oval.OvalEntityConstraint;
import com.example.scap.oval.CompiledOvalObjectPlan;

import java.util.Optional;

public class RegistryObjectPlan extends CompiledOvalObjectPlan {
    public Optional<OvalEntityConstraint> hive() {
        return findEntity("hive");
    }

    public Optional<OvalEntityConstraint> key() {
        return findEntity("key");
    }

    public Optional<OvalEntityConstraint> name() {
        return findEntity("name");
    }
}
