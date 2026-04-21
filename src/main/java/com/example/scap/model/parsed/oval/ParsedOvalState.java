package com.example.scap.model.parsed.oval;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ParsedOvalState {
    private String stateId;
    private String stateType;
    private String namespace;
    private final List<ParsedOvalEntity> entities = new ArrayList<>();
}
