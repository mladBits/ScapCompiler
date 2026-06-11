package com.touchstone.compiler.oval.common;

import com.touchstone.compiler.oval.CollectionTask;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectionTaskBase implements CollectionTask {
    private final String family;
    @Getter
    private final List<OvalFilterTask> filters;

    protected CollectionTaskBase(final String family) {
        this.family = family;
        this.filters = new ArrayList<>();
    }


    @Override
    public String getFamily() {
        return this.family;
    }
}
