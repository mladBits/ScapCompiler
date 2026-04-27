package com.example.scap.index;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfGroup;
import org.springframework.stereotype.Component;

@Component
public class XccdfBenchmarkIndexBuilder {
    public XccdfBenchmarkIndex build(final ParsedXccdfBenchmark benchmark) {
        final XccdfBenchmarkIndex index = new XccdfBenchmarkIndex();

        benchmark.getGroups().forEach(group -> indexGroup(group, index));
        benchmark.getRules().forEach(rule -> index.getRulesById().put(rule.getRuleId(), rule));
        benchmark.getValues()
                .forEach(var -> index.getValuesById().put(var.getId(), var));

        return index;
    }

    private void indexGroup(final ParsedXccdfGroup group, final XccdfBenchmarkIndex index) {
        index.getGroupsById().put(group.getGroupId(), group);
        group.getRules()
                .forEach(parsedXccdfRule ->
                        index.getRulesById().put(parsedXccdfRule.getRuleId(), parsedXccdfRule));

        group.getGroups().forEach(parsedXccdfGroup -> indexGroup(parsedXccdfGroup, index));
    }
}
