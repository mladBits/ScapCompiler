package com.example.scap.model.parsed.xccdf;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParsedXccdfBenchmark {
    private String benchmarkId;
    private String title;
    private String description;
    private List<ParsedXccdfProfile> profiles = new ArrayList<>();
    private List<ParsedXccdfGroup> groups = new ArrayList<>();
    private List<ParsedXccdfRule> rules = new ArrayList<>();
    private List<ParsedXccdfValue> values;
}
