package com.example.scap.model.parsed.xccdf;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedXccdfGroup {
    private String groupId;
    private String title;
    private final List<ParsedXccdfRule> rules = new ArrayList<>();
    private final List<ParsedXccdfGroup> groups = new ArrayList<>();
}
