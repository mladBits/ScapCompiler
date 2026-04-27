package com.example.scap.api.dto;

import lombok.Data;

@Data
public class CompileTemplateRequest {
    private String benchmarkId;
    private String profileId;

    //private Map<String, List<String>> variables = new HashMap<>();
}