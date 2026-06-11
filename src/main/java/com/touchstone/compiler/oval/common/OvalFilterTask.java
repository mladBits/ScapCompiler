package com.touchstone.compiler.oval.common;

import com.touchstone.compiler.oval.CollectionTask;
import com.touchstone.compiler.oval.EntitySelector;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OvalFilterTask implements CollectionTask {
    private String action;
    private String stateRef;
    private final List<EntitySelector> predicates = new ArrayList<>();

    @Override
    public String getFamily() {
        return "oval.filter";
    }
}
