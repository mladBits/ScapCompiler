package com.example.scap.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CompileTemplateRequest {
    private String benchmarkId;
    private String profileId;

    private Map<String, String> variables = new HashMap<>();
}