package com.example.scap.model.parsed.oval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOvalCriteria implements ParsedOvalCriteriaNode {
    private String operator;
    private List<ParsedOvalCriteriaNode> children = new ArrayList<>();
}
