package com.example.scap.oval.windows.registry;

import com.example.scap.oval.CompiledOvalCheck;
import lombok.Setter;

@Setter
public class CompiledRegistryCheck implements CompiledOvalCheck {
    private String testId;
    private String objectId;
    private String check;
    private String checkExistence;

    @Override
    public String testId() {
        return testId;
    }

    @Override
    public String family() {
        return "windows.registry";
    }
}
