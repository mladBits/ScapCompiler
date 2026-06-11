package com.example.scap.oval.common;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.compiled.CompiledState;
import com.example.scap.model.parsed.oval.*;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.OvalCheckCompiler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class CheckCompilerBase implements OvalCheckCompiler {
    protected abstract String supportedTestType();

    protected abstract ObjectCompilationResult compileSimpleObject(
            OvalCheckCompileContext context,
            ParsedOvalObject object);

    @Override
    public final boolean supports(final ParsedOvalTest test) {
        return test != null && supportedTestType().equals(test.getTestType());
    }

    /**
     * The OVAL object type this compiler can collect, derived from the test
     * type by convention (registry_test -> registry_object). Override when a
     * probe's element names do not follow the convention.
     */
    protected String supportedObjectType() {
        final String testType = supportedTestType();
        return testType.endsWith("_test")
                ? testType.substring(0, testType.length() - "_test".length()) + "_object"
                : testType + "_object";
    }

    @Override
    public final boolean supportsObject(final ParsedOvalObjectBase object) {
        return object != null && supportedObjectType().equals(object.getObjectType());
    }

    @Override
    public final ObjectCompilationResult compileObjectPlan(
            final OvalCheckCompileContext context,
            final ParsedOvalObjectBase object) {
        final ObjectCompilationResult result = compileObject(context, object, new HashSet<>());
        context.getObjects().putAll(result.getObjectsById());
        return result;
    }

    @Override
    public final Optional<CompiledOvalCheckBase> compile(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test) {

        if (!supports(test)) {
            return Optional.empty();
        }

        final ParsedOvalObjectBase object = requireObject(context.getOvalIndex(), test.getObjectRef());
        final ObjectCompilationResult result = compileObject(
                context,
                object,
                new HashSet<>());

        for (final String stateRef : test.getStateRef()) {
            final ParsedOvalState parsedState =
                    requireState(context.getOvalIndex(), stateRef);

            final CompiledState compiledState = compileState(parsedState);
            context.getStates().put(compiledState.getStateId(), compiledState);
        }

        context.getObjects().putAll(result.getObjectsById());

        return Optional.of(new CompiledOvalCheckBase(
                test.getId(),
                test.getObjectRef(),
                test.getStateRef(),
                test.getCheck(),
                test.getCheckExistence()
        ));
    }

    private ObjectCompilationResult compileObject (
            final OvalCheckCompileContext context,
            final ParsedOvalObjectBase object,
            final Set<String> visitedObjectIds) {
        if (!visitedObjectIds.add(object.getObjectId())) {
            throw new IllegalArgumentException("Circular object reference: " + object.getObjectId());
        }

        try {
            final ObjectCompilationResult result;
            if (object instanceof ParsedOvalObjectSet parsedOvalObjectSet) {
                result = compileSetObject(context, parsedOvalObjectSet, visitedObjectIds);
            } else if (object instanceof ParsedOvalObject parsedOvalObject) {
                result = compileSimpleObject(context, parsedOvalObject);
            } else {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }

            applyFilter(context, object, result);
            return result;
        } finally {
            visitedObjectIds.remove(object.getObjectId());
        }
    }

    protected CompiledState compileState(final ParsedOvalState state) {
        final CompiledState compiledState = new CompiledState();
        compiledState.setStateId(state.getStateId());
        compiledState.setStateType(state.getStateType());

        for (final ParsedOvalEntity entity : state.getEntities()) {
            compiledState.getAssertions().add(entity.resolve());
        }

        return compiledState;
    }

    private ObjectCompilationResult compileSetObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObjectSet parsedOvalObjectSet,
            final Set<String> visitedObjectIds) {
        final CompiledObjectPlan setPlan =
                CompiledObjectPlan.builder()
                        .objectId(parsedOvalObjectSet.getObjectId())
                        .objectType(parsedOvalObjectSet.getObjectType())
                        .tasks(new ArrayList<>())
                        .build();

        final ObjectCompilationResult result = new ObjectCompilationResult(parsedOvalObjectSet.getObjectId());
        final List<String> inputObjectIds  = new ArrayList<>();

        for (final String objectRef: parsedOvalObjectSet.getSet().getObjectRefs()) {
            final ParsedOvalObjectBase childObject = requireObject(context.getOvalIndex(), objectRef);
            final ObjectCompilationResult childResult = compileObject(context, childObject, visitedObjectIds);

            result.merge(childResult);
            inputObjectIds.add(childResult.getRootObjectId());
        }

        final OvalSetTask setTask = new OvalSetTask();
        setTask.setObjectId(parsedOvalObjectSet.getObjectId());
        setTask.setOperator(parsedOvalObjectSet.getSet().getOperator());
        setTask.getInputs().addAll(inputObjectIds);

        for (final ParsedOvalFilter filter: parsedOvalObjectSet.getSet().getFilters()) {
            final ParsedOvalState state = requireState(context.getOvalIndex(), filter.getStateRef());
            final OvalFilterTask filterTask = new OvalFilterTask();
            filterTask.setAction(filter.getAction());
            filterTask.setStateRef(filter.getStateRef());

            state.getEntities().forEach(entity -> filterTask.getPredicates().add(entity.resolve()));
            setTask.getFilters().add(filterTask);
        }
        setPlan.getTasks().add(setTask);

        result.addObject(setPlan);
        return result;
    }

    private void applyFilter(
            final OvalCheckCompileContext context,
            final ParsedOvalObjectBase object,
            final ObjectCompilationResult result) {
        if (object.getFilters() == null || object.getFilters().isEmpty()) {
            return;
        }

        for (final ParsedOvalFilter filter : object.getFilters()) {
            final ParsedOvalState state = requireState(context.getOvalIndex(), filter.getStateRef());
            final OvalFilterTask filterTask = new OvalFilterTask();
            filterTask.setAction(filter.getAction());
            filterTask.setStateRef(filter.getStateRef());
            state.getEntities().forEach(entity -> filterTask.getPredicates().add(entity.resolve()));
            result.getObjectsById().get(object.getObjectId()).getTasks().add(filterTask);
        }
    }

    protected ParsedOvalObjectBase requireObject(final OvalIndex ovalIndex, final String objectRef) {
        final ParsedOvalObjectBase object = ovalIndex.getObjectById().get(objectRef);

        if (object == null) {
            throw new IllegalArgumentException("OVAL object not found: " + objectRef);
        }

        return object;
    }

    protected ParsedOvalState requireState(final OvalIndex ovalIndex, final String stateRef) {
        final ParsedOvalState state = ovalIndex.getStateById().get(stateRef);

        if (state == null) {
            throw new IllegalArgumentException("OVAL state not found: " + stateRef);
        }

        return state;
    }
}
