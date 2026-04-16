package com.example.scap.model.parsed.oval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedOvalDefinitions {
    private List<ParsedOvalDefinition> definitions = new ArrayList<>();
}
