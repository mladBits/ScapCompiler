package com.example.scap.oval.windows.registry;

import com.example.scap.oval.CompiledOvalCheck;

import java.util.List;

public record CompiledRegistryCheck(
        String testId,
        RegistryObjectPlan objectPlan,
        List<RegistryStatePlan> statePlans
) implements CompiledOvalCheck {

    @Override
    public String family() {
        return "windows.registry";
    }
}
