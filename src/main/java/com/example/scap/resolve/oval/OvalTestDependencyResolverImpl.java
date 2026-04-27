package com.example.scap.resolve.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.ParsedOvalObjectBase;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OvalTestDependencyResolverImpl implements OvalTestDependencyResolver {

    @Override
    public Result resolve(final OvalIndex ovalIndex, final Collection<ParsedOvalTest> tests) {
        final Set<String> objectIds =
                tests.stream()
                        .map(ParsedOvalTest::getObjectRef)
                        .filter(object -> object != null && !object.isBlank())
                        .collect(Collectors.toSet());

        final Set<String> stateIds =
                tests.stream()
                        .flatMap(test -> test.getStateRef().stream())
                        .filter(state -> state != null && !state.isBlank())
                        .collect(Collectors.toSet());

        return new Result(
                objectIds.stream()
                        .map(objectId -> requireObject(ovalIndex, objectId))
                        .toList(),
                stateIds.stream()
                        .map(stateId -> requireState(ovalIndex, stateId))
                        .toList()
        );
    }

    private ParsedOvalObjectBase requireObject(final OvalIndex ovalIndex, final String objectId) {
        final ParsedOvalObjectBase object = ovalIndex.getObjectById().get(objectId);
        if (object == null) {
            throw new IllegalArgumentException("OVAL object not found: " + objectId);
        }
        return object;
    }

    private ParsedOvalState requireState(final OvalIndex ovalIndex, final String stateId) {
        final ParsedOvalState state = ovalIndex.getStateById().get(stateId);
        if (state == null) {
            throw new IllegalArgumentException("OVAL state not found: " + stateId);
        }
        return state;
    }
}
