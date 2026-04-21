package com.example.scap.index;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OvalIndex {
    private final Map<String, ParsedOvalDefinition> definitionById = new HashMap<>();
    private final Map<String, ParsedOvalTest> testById = new HashMap<>();
    private final Map<String, ParsedOvalObject> objectById = new HashMap<>();
    private final Map<String, ParsedOvalState> stateById = new HashMap<>();
}
