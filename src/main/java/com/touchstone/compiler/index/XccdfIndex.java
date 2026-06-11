package com.touchstone.compiler.index;

import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfGroup;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfRule;
import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfValue;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class XccdfIndex {
    private final Map<String, ParsedXccdfRule> rulesById = new HashMap<>();
    private final Map<String, ParsedXccdfGroup> groupsById = new HashMap<>();
    private final Map<String, ParsedXccdfValue> valuesById = new HashMap<>();
}
