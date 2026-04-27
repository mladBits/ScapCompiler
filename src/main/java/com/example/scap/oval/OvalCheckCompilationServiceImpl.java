package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.variables.ResolvedVariableBindings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OvalCheckCompilationServiceImpl implements OvalCheckCompilationService {
    private final List<OvalCheckCompiler> compilers;

    @Override
    public OvalCheckCompilationResult compile(
            final OvalIndex ovalIndex,
            final ResolvedOvalEvaluationSlice slice,
            final ResolvedVariableBindings bindings,
            final LocalVariableCompilationResult localVariables) {

        final OvalCheckCompileContext context =
                new OvalCheckCompileContext(
                        ovalIndex,
                        slice,
                        bindings,
                        localVariables,
                        new HashMap<>(),
                        new HashMap<>());

        final OvalCheckCompilationResult result = new OvalCheckCompilationResult();

        for (final ParsedOvalTest test : slice.getTests()) {
            try {
                final Optional<CompiledOvalCheck> compiled = compileOne(context, test);

                if (compiled.isPresent()) {
                    result.getCompiledChecks().add(compiled.get());
                } else {
                    result.getUnsupportedCheckTypes().add(test.getTestType());
                }
            } catch (final Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }

        result.getObjects().putAll(context.getObjects());
        result.getStates().putAll(context.getStates());
        return result;
    }

    private Optional<CompiledOvalCheck> compileOne(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test) {
        for (final OvalCheckCompiler compiler : compilers) {
            if (compiler.supports(test)) {
                return compiler.compile(context, test)
                        .map(check -> (CompiledOvalCheck) check);
            }
        }

        return Optional.empty();
    }
}
