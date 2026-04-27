package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.model.resolved.xccdf.ResolvedCheckReference;
import com.example.scap.model.resolved.xccdf.ResolvedRuleOvalRefs;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ReferencedOvalDefinitionResolverImpl implements ReferencedOvalDefinitionResolver {
    private final OvalEvaluationSliceResolver ovalEvaluationSliceResolver;

    @Override
    public ResolvedOvalEvaluationSlice resolve(final OvalIndex ovalIndex, final List<ResolvedRuleOvalRefs> ruleRefs) {
        final Set<String> startingDefinitionIds = ruleRefs.stream()
                .flatMap(ruleRef -> ruleRef.getReferences().stream())
                .map(ResolvedCheckReference::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        return ovalEvaluationSliceResolver.resolve(ovalIndex, startingDefinitionIds);
    }
}
