package com.touchstone.compiler.model.resolved.xccdf;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResolvedRuleOvalRefs {
    private String ruleId;
    private List<ResolvedCheckReference> references;
}
