package com.touchstone.compiler.resolve.oval;

import com.touchstone.compiler.index.OvalIndex;
import com.touchstone.compiler.model.parsed.oval.*;
import com.touchstone.compiler.model.parsed.oval.variables.*;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.touchstone.compiler.model.resolved.oval.ResolvedOvalVariableClosure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OvalVariableClosureResolverImpl implements OvalVariableClosureResolver {

    @Override
    public ResolvedOvalVariableClosure resolve(
            final OvalIndex ovalIndex,
            final ResolvedOvalEvaluationSlice slice) {
        final Walk walk = new Walk(ovalIndex);

        for (final ParsedOvalObjectBase object : slice.getObjects()) {
            walk.scanObject(object);
        }

        for (final ParsedOvalState state : slice.getStates()) {
            walk.scanState(state);
        }

        return walk.closure;
    }

    /**
     * Depth-first walk over the variable/object reference graph with cycle
     * detection on variables. Object revisits simply stop (circular object
     * references are rejected later by the check compiler).
     */
    @RequiredArgsConstructor
    private static final class Walk {
        private final OvalIndex ovalIndex;
        private final ResolvedOvalVariableClosure closure = new ResolvedOvalVariableClosure();
        private final Set<String> visitedVariableIds = new HashSet<>();
        private final Set<String> inProgressVariableIds = new LinkedHashSet<>();
        private final Set<String> visitedObjectIds = new HashSet<>();

        void scanObject(final ParsedOvalObjectBase object) {
            if (object == null || !visitedObjectIds.add(object.getObjectId())) {
                return;
            }

            if (object instanceof ParsedOvalObject simpleObject) {
                scanEntities(simpleObject.getEntities());
                if ("variable_object".equals(simpleObject.getObjectType())) {
                    scanVariableObjectRef(simpleObject);
                }
            } else if (object instanceof ParsedOvalObjectSet setObject && setObject.getSet() != null) {
                scanSet(setObject.getSet());
            }

            scanFilters(object.getFilters());
        }

        private void scanSet(final ParsedOvalSet set) {
            for (final String objectRef : set.getObjectRefs()) {
                scanObject(ovalIndex.getObjectById().get(objectRef));
            }

            scanFilters(set.getFilters());
            set.getChildSets().forEach(this::scanSet);
        }

        void scanState(final ParsedOvalState state) {
            if (state != null) {
                scanEntities(state.getEntities());
            }
        }

        private void scanFilters(final List<ParsedOvalFilter> filters) {
            if (filters == null) {
                return;
            }

            for (final ParsedOvalFilter filter : filters) {
                scanState(ovalIndex.getStateById().get(filter.getStateRef()));
            }
        }

        private void scanEntities(final List<ParsedOvalEntity> entities) {
            for (final ParsedOvalEntity entity : entities) {
                final String varRef = entity.getAttributes().get("var_ref");
                if (varRef != null && !varRef.isBlank()) {
                    visitVariable(varRef);
                }
            }
        }

        /**
         * The independent-family variable_object carries its variable
         * reference as the var_ref entity's text, not as a var_ref attribute.
         */
        private void scanVariableObjectRef(final ParsedOvalObject object) {
            object.getEntities().stream()
                    .filter(entity -> "var_ref".equals(entity.getName()))
                    .map(ParsedOvalEntity::getValue)
                    .filter(value -> value != null && !value.isBlank())
                    .forEach(this::visitVariable);
        }

        private void visitVariable(final String variableId) {
            if (visitedVariableIds.contains(variableId)) {
                return;
            }

            if (inProgressVariableIds.contains(variableId)) {
                markUnsupported(variableId, "circular variable reference: "
                        + String.join(" -> ", inProgressVariableIds) + " -> " + variableId);
                return;
            }

            closure.getVariableIds().add(variableId);

            final ParsedOvalVariable variable = ovalIndex.getVariableById().get(variableId);
            if (variable == null) {
                markUnsupported(variableId, "variable not found: " + variableId);
                visitedVariableIds.add(variableId);
                return;
            }

            if (variable instanceof ParsedOvalLocalVariable localVariable) {
                inProgressVariableIds.add(variableId);
                try {
                    walkComponent(variableId, localVariable.getExpression());
                } finally {
                    inProgressVariableIds.remove(variableId);
                }
            }

            visitedVariableIds.add(variableId);
        }

        private void walkComponent(final String variableId, final ParsedOvalVariableComponent component) {
            switch (component) {
                case null -> markUnsupported(variableId, "local variable has no expression: " + variableId);
                case ParsedLiteralComponent ignored -> { }
                case ParsedVariableComponent variableComponent -> visitVariable(variableComponent.getVarRef());
                case ParsedObjectComponent objectComponent -> {
                    closure.getObjectIds().add(objectComponent.getObjectRef());
                    closure.getObjectIdsByVariableId()
                            .computeIfAbsent(variableId, key -> new LinkedHashSet<>())
                            .add(objectComponent.getObjectRef());
                    scanObject(ovalIndex.getObjectById().get(objectComponent.getObjectRef()));
                }
                case ParsedConcatComponent concat ->
                        concat.getComponents().forEach(child -> walkComponent(variableId, child));
                case ParsedRegexCaptureComponent regexCapture ->
                        walkComponent(variableId, regexCapture.getComponent());
                case ParsedUnsupportedComponent unsupported ->
                        markUnsupported(variableId, "unsupported variable function '"
                                + unsupported.getFunctionName() + "' in " + variableId);
            }
        }

        private void markUnsupported(final String variableId, final String reason) {
            log.warn(reason);
            closure.getVariableIds().add(variableId);
            closure.getUnsupportedVariableReasons().putIfAbsent(variableId, reason);
        }
    }
}