package com.touchstone.compiler.model.compiled;

import com.touchstone.compiler.oval.EntitySelector;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompiledState {
    private String stateId;
    private String stateType;
    private final List<EntitySelector> assertions = new ArrayList<>();
}
