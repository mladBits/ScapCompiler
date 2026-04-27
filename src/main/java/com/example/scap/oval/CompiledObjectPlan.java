package com.example.scap.oval;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompiledObjectPlan {
    private String objectId;
    private String objectType;
    private final List<CollectionTask> tasks;
}
