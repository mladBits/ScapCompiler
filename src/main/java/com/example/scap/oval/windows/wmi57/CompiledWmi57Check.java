package com.example.scap.oval.windows.wmi57;

import com.example.scap.oval.CompiledOvalCheck;
import lombok.Setter;

@Setter
public class CompiledWmi57Check implements CompiledOvalCheck  {
    private String testId;

    @Override
    public String testId() {
        return testId;
    }

    @Override
    public String family() {
        return "windows.wmi57";
    }
}
