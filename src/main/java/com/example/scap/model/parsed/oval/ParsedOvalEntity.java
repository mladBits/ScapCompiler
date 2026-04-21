package com.example.scap.model.parsed.oval;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ParsedOvalEntity {
    private String name;
    private String value;
    private final Map<String, String> attributes = new HashMap<>();
}
