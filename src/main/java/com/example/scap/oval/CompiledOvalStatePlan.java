package com.example.scap.oval;

import com.example.scap.model.normalized.oval.OvalEntityConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CompiledOvalStatePlan {
    private String stateId;
    private String stateType;
    private String namespace;

    private final List<OvalEntityConstraint> entities = new ArrayList<>();

    public Optional<OvalEntityConstraint> findEntity(final String entityName) {
        return entities.stream()
                .filter(entity -> entityName.equals(entity.getEntityName()))
                .findFirst();
    }

    public List<OvalEntityConstraint> findEntities(final String entityName) {
        return entities.stream()
                .filter(entity -> entityName.equals(entity.getEntityName()))
                .toList();
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(final String stateId) {
        this.stateId = stateId;
    }

    public String getStateType() {
        return stateType;
    }

    public void setStateType(final String stateType) {
        this.stateType = stateType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public List<OvalEntityConstraint> getEntities() {
        return entities;
    }
}
