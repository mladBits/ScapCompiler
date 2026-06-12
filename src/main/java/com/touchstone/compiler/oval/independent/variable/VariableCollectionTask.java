package com.touchstone.compiler.oval.independent.variable;

import com.touchstone.compiler.oval.common.CollectionTaskBase;
import lombok.Getter;

/**
 * No host probe: the agent "collects" by resolving the referenced variable
 * (from the template's variablesById) and emitting one item per value.
 */
@Getter
public class VariableCollectionTask extends CollectionTaskBase {

    private final String variableId;

    protected VariableCollectionTask(final String variableId) {
        super("independent.variable");
        this.variableId = variableId;
    }
}
