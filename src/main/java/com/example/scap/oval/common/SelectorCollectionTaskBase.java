package com.example.scap.oval.common;

import com.example.scap.oval.EntitySelector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SelectorCollectionTaskBase extends CollectionTaskBase {
    private final List<EntitySelector> selectors = new ArrayList<>();

    protected SelectorCollectionTaskBase(final String family) {
        super(family);
    }

    public void addSelector(final EntitySelector selector) {
        selectors.add(selector);
    }
}
