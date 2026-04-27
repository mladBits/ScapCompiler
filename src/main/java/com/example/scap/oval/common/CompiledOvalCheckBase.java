package com.example.scap.oval.common;

import com.example.scap.oval.CompiledOvalCheck;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompiledOvalCheckBase implements CompiledOvalCheck  {
    private String testId;
    private String objectId;
    private List<String> stateIds;
    private String check;
    private String checkExistence;
}
