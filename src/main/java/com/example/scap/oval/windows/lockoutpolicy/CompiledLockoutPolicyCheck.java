package com.example.scap.oval.windows.lockoutpolicy;

import com.example.scap.oval.CompiledOvalCheck;
import lombok.Setter;

@Setter
public class CompiledLockoutPolicyCheck implements CompiledOvalCheck  {
    private String testId;

    @Override
    public String testId() {
        return testId;
    }

    @Override
    public String family() {
        return "windows.lockout_policy";
    }
}
