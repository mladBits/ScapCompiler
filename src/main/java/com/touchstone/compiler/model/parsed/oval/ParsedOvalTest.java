package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOvalTest {
    private String testType;
    private String id;
    private String checkExistence;
    private String check;
    private String state_operator;
    private String objectRef;
    private List<String> stateRef = new ArrayList<>();
}
