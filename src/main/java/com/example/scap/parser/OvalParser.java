package com.example.scap.parser;

import java.util.List;

public interface OvalParser {
    ParsedOval parse(byte[] xmlBytes);

    record ParsedOval(
            List<String> definitionIds,
            List<ParsedExternalVariable> externalVariables
    ) {
    }

    record ParsedExternalVariable(
            String id,
            String datatype
    ) {
    }
}