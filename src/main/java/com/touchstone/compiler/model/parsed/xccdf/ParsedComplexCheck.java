package com.touchstone.compiler.model.parsed.xccdf;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedComplexCheck implements ParsedCheckNode {
    private String operator;
    private List<ParsedCheckNode> children = new ArrayList<>();
}
