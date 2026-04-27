package com.example.scap.oval.common;

import com.example.scap.oval.CollectionTask;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectionTaskBase implements CollectionTask {
    private final String family;
    private final List<OvalFilterTask> filters;

    protected CollectionTaskBase(final String family) {
        this.family = family;
        this.filters = new ArrayList<>();
    }

    @Override
    public List<OvalFilterTask> getFilters() {
        return this.filters;
    }

    @Override
    public String getFamily() {
        return this.family;
    }
}
