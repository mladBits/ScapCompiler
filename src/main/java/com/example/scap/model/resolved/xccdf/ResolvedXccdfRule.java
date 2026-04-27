package com.example.scap.model.resolved.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
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
