package com.example.scap.variables;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ResolvedVariableBindings {
    private final Map<String, VariableBinding> bindingsById = new HashMap<>();
    private final List<String> UnresolvedVariableIds = new ArrayList<>();
}
