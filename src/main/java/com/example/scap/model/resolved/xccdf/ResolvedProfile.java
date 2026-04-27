package com.example.scap.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolvedProfile {
    private String benchmarkId;
    private String profileId;
    private List<ResolvedXccdfRule> selectedRules = new ArrayList<>();
}
