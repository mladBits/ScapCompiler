package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OvalCheckCompilationServiceImpl implements OvalCheckCompilationService {

    private final List<OvalCheckCompiler<? extends CompiledOvalCheck>> compilers;

    @Override
    public OvalCheckCompilationResult compile(
            final OvalIndex ovalIndex,
            final ResolvedOvalEvaluationSlice slice
    ) {
        final OvalCheckCompileContext context = new OvalCheckCompileContext(ovalIndex, slice);

        final OvalCheckCompilationResult result =
                new OvalCheckCompilationResult();

        for (final ParsedOvalTest test : slice.getTests()) {
            final Optional<CompiledOvalCheck> compiled = compileOne(context, test);

            if (compiled.isPresent()) {
                result.getCompiledChecks().add(compiled.get());
            } else {
                result.getUnsupportedTestIds().add(test.getId());
            }
        }

        return result;
    }

    private Optional<CompiledOvalCheck> compileOne(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test
    ) {
        for (final OvalCheckCompiler<? extends CompiledOvalCheck> compiler : compilers) {
            if (compiler.supports(test)) {
                return compiler.compile(context, test)
                        .map(check -> (CompiledOvalCheck) check);
            }
        }

        return Optional.empty();
    }
}
