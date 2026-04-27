package com.example.scap.oval.common;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.*;
import com.example.scap.oval.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class CheckCompilerBase<T extends CompiledOvalCheck> implements OvalCheckCompiler<T> {
    protected abstract String supportedTestType();

    protected abstract ObjectCompilationResult compileSimpleObject(
            OvalCheckCompileContext context,
            ParsedOvalObject object);

    protected abstract Optional<T> compileResolved(
            OvalCheckCompileContext context,
            ParsedOvalTest test,
            List<ParsedOvalState> states,
            ObjectCompilationResult objectResult
    );

    @Override
    public final boolean supports(final ParsedOvalTest test) {
        return test != null && supportedTestType().equals(test.getTestType());
    }

    @Override
    public final Optional<T> compile(
            final OvalCheckCompileContext context,
            final ParsedOvalTest test) {
        if (!supports(test)) {
            return Optional.empty();
        }

        final ParsedOvalObjectBase object = requireObject(context.getOvalIndex(), test.getObjectRef());
        final List<ParsedOvalState> states = test.getStateRef().stream()
                .map(stateRef -> requireState(context.getOvalIndex(), stateRef))
                .toList();

        final ObjectCompilationResult objectResult = compileObject(
                context,
                object,
                new HashSet<>());

        context.getObjects().putAll(objectResult.getObjectsById());
        return compileResolved(context, test, states, objectResult);
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
        final List<String> inputTaskIds = new ArrayList<>();

        for (final String objectRef: parsedOvalObjectSet.getSet().getObjectRefs()) {
            final ParsedOvalObjectBase childObject = requireObject(context.getOvalIndex(), objectRef);
            final ObjectCompilationResult childResult = compileObject(context, childObject, visitedObjectIds);

            result.merge(childResult);
            inputTaskIds.add(childResult.getRootObjectId());
        }

        final OvalSetTask setTask = new OvalSetTask();
        setTask.setObjectId(parsedOvalObjectSet.getObjectId());
        setTask.setOperator(parsedOvalObjectSet.getSet().getOperator());
        setTask.getInputs().addAll(inputTaskIds);

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
