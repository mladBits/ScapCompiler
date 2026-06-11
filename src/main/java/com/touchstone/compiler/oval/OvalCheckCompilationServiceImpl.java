package com.touchstone.compiler.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.compiled.variables.LocalVariableCompilationResult;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalObjectBase;
import com.touchstone.compiler.model.parsed.oval.ParsedOvalTest;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.variables.ResolvedVariableBindings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
            final LocalVariableCompilationResult localVariables,
            final Collection<String> variableReferencedObjectIds) {

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

        compileVariableReferencedObjects(context, variableReferencedObjectIds, result);

        result.getObjects().putAll(context.getObjects());
        result.getStates().putAll(context.getStates());
        return result;
    }

    /**
     * Objects referenced only by local variable object components have no test
     * to drive their compilation, but the agent still has to collect them.
     */
    private void compileVariableReferencedObjects(
            final OvalCheckCompileContext context,
            final Collection<String> objectIds,
            final OvalCheckCompilationResult result) {
        if (objectIds == null) {
            return;
        }

        for (final String objectId : objectIds) {
            if (context.getObjects().containsKey(objectId)) {
                continue;
            }

            final ParsedOvalObjectBase object = context.getOvalIndex().getObjectById().get(objectId);
            if (object == null) {
                recordFailedObject(result, objectId, "OVAL object not found: " + objectId);
                continue;
            }

            final Optional<OvalCheckCompiler> compiler = compilers.stream()
                    .filter(candidate -> candidate.supportsObject(object))
                    .findFirst();

            if (compiler.isEmpty()) {
                recordFailedObject(result, objectId,
                        "No compiler for variable-referenced object type: " + object.getObjectType());
                continue;
            }

            try {
                compiler.get().compileObjectPlan(context, object);
            } catch (final Exception e) {
                recordFailedObject(result, objectId,
                        "Failed to compile variable-referenced object " + objectId + ": " + e.getMessage());
            }
        }
    }

    private void recordFailedObject(
            final OvalCheckCompilationResult result,
            final String objectId,
            final String warning) {
        log.warn(warning);
        result.getFailedObjectIds().add(objectId);
        result.getWarnings().add(warning);
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