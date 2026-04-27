package com.example.scap.resolve.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedCheckNode;
import com.example.scap.model.parsed.xccdf.ParsedCheckReference;
import com.example.scap.model.parsed.xccdf.ParsedComplexCheck;
import com.example.scap.model.resolved.xccdf.ResolveCheckExport;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CheckReferenceCollector {
    public List<ResolvedCheckReference> collect(final ParsedCheckNode node) {
        final Set<ResolvedCheckReference> collected = new HashSet<>();
        collectInto(node, collected);
        return new ArrayList<>(collected);
    }

    public List<ResolvedCheckReference> collect(final List<ParsedCheckNode> nodes) {
        final Set<ResolvedCheckReference> collected = new HashSet<>();
        nodes.forEach(node -> collectInto(node, collected));
        return new ArrayList<>(collected);
    }

    private void collectInto(final ParsedCheckNode node,
                             final Set<ResolvedCheckReference> collected) {
        if (node instanceof ParsedCheckReference parsedCheckReference) {
            final List<ResolveCheckExport> parsedCheckExports =
                    parsedCheckReference.getCheckExports().stream()
                            .map(parsed -> new ResolveCheckExport(parsed.getExportName(), parsed.getValueId()))
                            .toList();

            final ResolvedCheckReference resolved = new ResolvedCheckReference(
                    nullSafe(parsedCheckReference.getSystem()),
                    nullSafe(parsedCheckReference.getHref()),
                    nullSafe(parsedCheckReference.getName()),
                    parsedCheckExports);

            collected.add(resolved);

        } else if (node instanceof ParsedComplexCheck parsedComplexCheck) {
            for (final ParsedCheckNode child : parsedComplexCheck.getChildren()) {
                collectInto(child, collected);
            }
        }
    }

    private String nullSafe(final String value) {
        return value == null ? "" : value;
    }
}
