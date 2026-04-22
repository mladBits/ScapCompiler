package com.example.scap.oval;

import com.example.scap.model.normalized.oval.OvalEntityConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CompiledOvalObjectPlan {
    private String objectId;
    private String objectType;
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

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(final String objectType) {
        this.objectType = objectType;
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
