package com.example.scap.oval.windows.passwordpolicy;

import com.example.scap.oval.CompiledOvalCheck;
import lombok.Setter;

@Setter
public class CompiledPasswordPolicyCheck implements CompiledOvalCheck  {
    private String testId;

    @Override
    public String testId() {
        return testId;
    }

    @Override
    public String family() {
        return "windows.password_policy";
    }
}
