package com.example.scap.model.parsed.xccdf;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedXccdfProfile {
    private String profileId;
    private String title;
    private List<String> selectedRuleIds = new ArrayList<>();
}
