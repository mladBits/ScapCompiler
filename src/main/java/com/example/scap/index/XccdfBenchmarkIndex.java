package com.example.scap.index;

import com.example.scap.model.parsed.xccdf.ParsedXccdfGroup;
import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class XccdfBenchmarkIndex {
    private final Map<String, ParsedXccdfRule> rulesById = new HashMap<>();
    private final Map<String, ParsedXccdfGroup> groupsById = new HashMap<>();
}
