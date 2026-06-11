package com.touchstone.compiler.oval.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OvalSetTask extends CollectionTaskBase {
    private String objectId;
    private String operator;
    private List<String> inputs = new ArrayList<>();

    public OvalSetTask() {
        super("oval.set");
    }
}
