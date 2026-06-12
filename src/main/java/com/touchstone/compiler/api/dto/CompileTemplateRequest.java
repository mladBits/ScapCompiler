package com.touchstone.compiler.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompileTemplateRequest {

    /**
     * Content package key: the directory name under packages/ in the
     * raw-content bucket.
     */
    private String packageId;

    /**
     * XCCDF profile ids within the package to compile. Optional: when null or
     * empty, every profile in the package is compiled.
     */
    private List<String> profileIds;
}
