package com.example.scap.model.parsed.xccdf;

import lombok.Data;

@Data
public class ParsedXccdfValue {
    private String id;
    private String title;
    private String type;
    private String defaultValue;
    private String value;
    private String operator;
}
