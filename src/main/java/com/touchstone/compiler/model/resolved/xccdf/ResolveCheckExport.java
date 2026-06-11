package com.touchstone.compiler.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolveCheckExport {
    private String exportName;
    private String valueId;
}
