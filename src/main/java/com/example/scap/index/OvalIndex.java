package com.example.scap.index;

import com.example.scap.model.parsed.oval.ParsedOvalDefinition;
import com.example.scap.model.parsed.oval.ParsedOvalObjectBase;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.model.parsed.oval.variables.ParsedOvalVariable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OvalIndex {
    private final Map<String, ParsedOvalDefinition> definitionById = new HashMap<>();
    private final Map<String, ParsedOvalTest> testById = new HashMap<>();
    private final Map<String, ParsedOvalObjectBase> objectById = new HashMap<>();
    private final Map<String, ParsedOvalState> stateById = new HashMap<>();
    private final Map<String, ParsedOvalVariable> variableById = new HashMap<>();
}
