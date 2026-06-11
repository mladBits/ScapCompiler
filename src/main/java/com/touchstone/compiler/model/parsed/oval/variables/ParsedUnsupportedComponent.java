package com.touchstone.compiler.model.parsed.oval.variables;

import lombok.Data;

/**
 * Placeholder for an OVAL variable function the compiler does not implement yet
 * (split, substring, arithmetic, ...). Parsing must survive these; the compile
 * stage marks the owning variable unresolved instead of failing the template.
 */
@Data
public final class ParsedUnsupportedComponent implements ParsedOvalVariableComponent {
    private String functionName;
}