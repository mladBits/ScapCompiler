package com.example.scap.parser;

import java.util.List;

public interface XccdfParser {
    ParsedXccdf parse(byte[] xmlBytes);

    record ParsedXccdf(
            String benchmarkId,
            List<String> profileIds,
            List<ParsedValue> values
    ) {
    }

    record ParsedValue(
            String id,
            String title,
            String type,
            String defaultValue
    ) {
    }
}
