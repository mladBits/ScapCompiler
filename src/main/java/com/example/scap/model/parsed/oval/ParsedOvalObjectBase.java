package com.example.scap.model.parsed.oval;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public abstract class ParsedOvalObjectBase {
    private String objectId;
    private String objectType;
    private String namespace;

    private final List<ParsedOvalFilter> filters = new ArrayList<>();
}
