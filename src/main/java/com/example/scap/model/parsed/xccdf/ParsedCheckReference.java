package com.example.scap.model.parsed.xccdf;

import lombok.Data;

@Data
public class ParsedCheckReference implements ParsedCheckNode {
    private String system;
    private String href;
    private String name;
}
