package com.example.scap.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class ResolvedCheckReference {
    private String system;
    private String href;
    private String name;
    private List<ResolveCheckExport> checkExports = new ArrayList<>();
}
