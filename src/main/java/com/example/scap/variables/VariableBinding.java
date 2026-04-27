package com.example.scap.variables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableBinding {
    private String variableId;
    private List<String> values;
    private VariableBindingSource source;
}
