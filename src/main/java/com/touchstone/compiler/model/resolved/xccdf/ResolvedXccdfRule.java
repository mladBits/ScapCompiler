package com.touchstone.compiler.model.resolved.xccdf;

import com.touchstone.compiler.model.parsed.xccdf.ParsedCheckNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolvedXccdfRule {
    private String ruleId;
    private String title;
    private List<ParsedCheckNode> checkNodes;
}
