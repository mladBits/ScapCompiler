package com.example.scap.model.parsed.xccdf;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedXccdfRule {
    private String ruleId;
    private String title;
    private List<ParsedCheckNode> checkReferences = new ArrayList<>();
}
