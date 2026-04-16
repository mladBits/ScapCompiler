package com.example.scap.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class ResolvedProfile {
    private String benchmarkId;
    private String profileId;
    private List<ResolvedXccdfRule> selectedRules;
}
