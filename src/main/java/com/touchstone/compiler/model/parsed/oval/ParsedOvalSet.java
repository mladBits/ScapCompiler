package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOvalSet {
    private String operator;
    private final List<String> objectRefs = new ArrayList<>();
    private final List<ParsedOvalFilter> filters = new ArrayList<>();
    private final List<ParsedOvalSet> childSets = new ArrayList<>();
}
