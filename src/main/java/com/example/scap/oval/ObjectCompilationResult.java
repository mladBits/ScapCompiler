package com.example.scap.oval;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class ObjectCompilationResult {
    private String rootObjectId;
    private final Map<String, CompiledObjectPlan> objectsById;

    public ObjectCompilationResult(final String rootObjectId) {
        this.rootObjectId = rootObjectId;
        this.objectsById = new HashMap<>();
    }

    public ObjectCompilationResult(final String objectId, final CompiledObjectPlan plan) {
        this.rootObjectId = objectId;
        this.objectsById = new HashMap<>();
        this.objectsById.put(objectId, plan);
    }

    public void merge(final ObjectCompilationResult other) {
        if (other == null) return;

        for (final CompiledObjectPlan plan: other.getObjectsById().values()) {
            this.objectsById.putIfAbsent(plan.getObjectId(), plan);
        }
    }

    public void addObject(final CompiledObjectPlan plan) {
        if (plan == null) {
            return;
        }

        objectsById.putIfAbsent(plan.getObjectId(), plan);
    }

    public void addTask(final String objectId, final CollectionTask collectionTask) {
        if (objectId.isBlank() || collectionTask == null) return;
        objectsById.get(objectId).getTasks().add(collectionTask);
    }
}
