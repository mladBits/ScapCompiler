package com.example.scap.oval.common;

import com.example.scap.oval.CollectionTask;
import com.example.scap.oval.EntitySelector;
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
