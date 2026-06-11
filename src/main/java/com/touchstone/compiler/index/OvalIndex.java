package com.touchstone.compiler.index;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalDefinition;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObjectBase;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalState;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.parsed.oval.variables.ParsedOvalVariable;
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
