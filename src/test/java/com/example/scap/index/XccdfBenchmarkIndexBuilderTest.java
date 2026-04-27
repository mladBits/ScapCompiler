package com.example.scap.index;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.parsed.xccdf.ParsedXccdfGroup;
import com.example.scap.model.parsed.xccdf.ParsedXccdfRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class XccdfBenchmarkIndexBuilderTest {
    private final XccdfIndexBuilder indexBuilder = new XccdfIndexBuilder();

    @Test
    void build_shouldIndexTopLevelGroupsById() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup group1 = group("group-1");
        ParsedXccdfGroup group2 = group("group-2");

        benchmark.getGroups().add(group1);
        benchmark.getGroups().add(group2);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(2, index.getGroupsById().size());
        assertSame(group1, index.getGroupsById().get("group-1"));
        assertSame(group2, index.getGroupsById().get("group-2"));
    }

    @Test
    void build_shouldIndexRulesDirectlyUnderTopLevelGroups() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup group = group("group-1");
        ParsedXccdfRule rule1 = rule("rule-1");
        ParsedXccdfRule rule2 = rule("rule-2");

        group.getRules().add(rule1);
        group.getRules().add(rule2);
        benchmark.getGroups().add(group);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(2, index.getRulesById().size());
        assertSame(rule1, index.getRulesById().get("rule-1"));
        assertSame(rule2, index.getRulesById().get("rule-2"));
    }

    @Test
    void build_shouldIndexTopLevelBenchmarkRules() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfRule topLevelRule1 = rule("rule-1");
        ParsedXccdfRule topLevelRule2 = rule("rule-2");

        benchmark.getRules().add(topLevelRule1);
        benchmark.getRules().add(topLevelRule2);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(2, index.getRulesById().size());
        assertSame(topLevelRule1, index.getRulesById().get("rule-1"));
        assertSame(topLevelRule2, index.getRulesById().get("rule-2"));
    }

    @Test
    void build_shouldIndexBothGroupRulesAndTopLevelRules() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup group = group("group-1");
        ParsedXccdfRule groupRule = rule("group-rule");
        ParsedXccdfRule topLevelRule = rule("top-rule");

        group.getRules().add(groupRule);
        benchmark.getGroups().add(group);
        benchmark.getRules().add(topLevelRule);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(1, index.getGroupsById().size());
        assertEquals(2, index.getRulesById().size());
        assertSame(groupRule, index.getRulesById().get("group-rule"));
        assertSame(topLevelRule, index.getRulesById().get("top-rule"));
    }

    @Test
    void build_shouldPreferTopLevelBenchmarkRuleWhenDuplicateRuleIdExists() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup group = group("group-1");
        ParsedXccdfRule groupRule = rule("duplicate-rule");
        groupRule.setTitle("group version");
        group.getRules().add(groupRule);

        ParsedXccdfRule topLevelRule = rule("duplicate-rule");
        topLevelRule.setTitle("top-level version");

        benchmark.getGroups().add(group);
        benchmark.getRules().add(topLevelRule);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(1, index.getRulesById().size());
        assertSame(topLevelRule, index.getRulesById().get("duplicate-rule"));
        assertEquals("top-level version", index.getRulesById().get("duplicate-rule").getTitle());
    }

    @Test
    void build_shouldIndexNestedChildGroups() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup parentGroup = group("parent-group");
        ParsedXccdfGroup childGroup = group("child-group");
        ParsedXccdfGroup grandchildGroup = group("grandchild-group");

        childGroup.getGroups().add(grandchildGroup);
        parentGroup.getGroups().add(childGroup);
        benchmark.getGroups().add(parentGroup);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(3, index.getGroupsById().size());
        assertSame(parentGroup, index.getGroupsById().get("parent-group"));
        assertSame(childGroup, index.getGroupsById().get("child-group"));
        assertSame(grandchildGroup, index.getGroupsById().get("grandchild-group"));
    }

    @Test
    void build_shouldIndexRulesInsideNestedChildGroups() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup parentGroup = group("parent-group");
        ParsedXccdfGroup childGroup = group("child-group");
        ParsedXccdfGroup grandchildGroup = group("grandchild-group");

        ParsedXccdfRule childRule = rule("child-rule");
        ParsedXccdfRule grandchildRule = rule("grandchild-rule");

        childGroup.getRules().add(childRule);
        grandchildGroup.getRules().add(grandchildRule);

        childGroup.getGroups().add(grandchildGroup);
        parentGroup.getGroups().add(childGroup);
        benchmark.getGroups().add(parentGroup);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(2, index.getRulesById().size());
        assertSame(childRule, index.getRulesById().get("child-rule"));
        assertSame(grandchildRule, index.getRulesById().get("grandchild-rule"));
    }

    @Test
    void build_shouldIndexMixedNestedStructure() {
        ParsedXccdfBenchmark benchmark = new ParsedXccdfBenchmark();

        ParsedXccdfGroup parentGroup = group("parent-group");
        ParsedXccdfGroup childGroupA = group("child-group-a");
        ParsedXccdfGroup childGroupB = group("child-group-b");

        ParsedXccdfRule parentRule = rule("parent-rule");
        ParsedXccdfRule childRuleA = rule("child-rule-a");
        ParsedXccdfRule childRuleB = rule("child-rule-b");
        ParsedXccdfRule topLevelRule = rule("top-level-rule");

        parentGroup.getRules().add(parentRule);
        childGroupA.getRules().add(childRuleA);
        childGroupB.getRules().add(childRuleB);

        parentGroup.getGroups().add(childGroupA);
        parentGroup.getGroups().add(childGroupB);

        benchmark.getGroups().add(parentGroup);
        benchmark.getRules().add(topLevelRule);

        XccdfIndex index = indexBuilder.build(benchmark);

        assertEquals(3, index.getGroupsById().size());
        assertEquals(4, index.getRulesById().size());

        assertSame(parentRule, index.getRulesById().get("parent-rule"));
        assertSame(childRuleA, index.getRulesById().get("child-rule-a"));
        assertSame(childRuleB, index.getRulesById().get("child-rule-b"));
        assertSame(topLevelRule, index.getRulesById().get("top-level-rule"));
    }

    private ParsedXccdfGroup group(String groupId) {
        ParsedXccdfGroup group = new ParsedXccdfGroup();
        group.setGroupId(groupId);
        return group;
    }

    private ParsedXccdfRule rule(String ruleId) {
        ParsedXccdfRule rule = new ParsedXccdfRule();
        rule.setRuleId(ruleId);
        return rule;
    }
}