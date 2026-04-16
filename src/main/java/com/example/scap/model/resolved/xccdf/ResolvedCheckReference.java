package com.example.scap.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ResolvedCheckReference {
    private String system;
    private String href;
    private String name;
}
