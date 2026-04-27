package com.example.scap.variables;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class LiteralEntityValue implements VariableAwareEntityValue {
    private String value;
}