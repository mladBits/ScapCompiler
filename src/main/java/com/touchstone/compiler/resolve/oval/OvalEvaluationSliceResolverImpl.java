package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class OvalEvaluationSliceResolverImpl implements OvalEvaluationSliceResolver {
    private final OvalDefinitionClosureResolver definitionClosureResolver;
    private final OvalTestDependencyResolver testDependencyResolver;

    @Override
    public ResolvedOvalEvaluationSlice resolve(final OvalIndex ovalIndex, final Collection<String> startingDefinitionIds) {
        final ResolvedOvalEvaluationSlice definitionSlice = definitionClosureResolver.resolve(ovalIndex, startingDefinitionIds);
        final OvalTestDependencyResolver.Result dependencies = testDependencyResolver.resolve(ovalIndex, definitionSlice.getTests());
        return new ResolvedOvalEvaluationSlice(
                definitionSlice.getDefinitions(),
                definitionSlice.getTests(),
                dependencies.objects(),
                dependencies.states()
        );
    }
}
