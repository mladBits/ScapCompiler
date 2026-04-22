package com.example.scap.oval.windows.registry;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.model.parsed.oval.ParsedOvalTest;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.OvalCheckCompiler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegistryCheckCompiler implements OvalCheckCompiler<CompiledRegistryCheck> {
    private final RegistryObjectPlanMapper objectMapper;
    private final RegistryStatePlanMapper stateMapper;

    @Override
    public boolean supports(ParsedOvalTest test) {
        return "registry_test".equals(test.getTestType());
    }

    @Override
    public Optional<CompiledRegistryCheck> compile(OvalCheckCompileContext context, ParsedOvalTest test) {
        if (!supports(test)) {
            return Optional.empty();
        }

        ParsedOvalObject object = requireObject(context.getOvalIndex(), test.getObjectRef());

        List<RegistryStatePlan> states = test.getStateRef().stream()
                .map(stateRef -> requireState(context.getOvalIndex(), stateRef))
                .map(stateMapper::map)
                .toList();

        return Optional.of(new CompiledRegistryCheck(
                test.getId(),
                objectMapper.map(object),
                states
        ));
    }

    private ParsedOvalObject requireObject(final OvalIndex ovalIndex, final String objectRef) {
        ParsedOvalObject object = ovalIndex.getObjectById().get(objectRef);

        if (object == null) {
            throw new IllegalArgumentException("OVAL object not found: " + objectRef);
        }

        return object;
    }

    private ParsedOvalState requireState(final OvalIndex ovalIndex, final String stateRef) {
        ParsedOvalState state = ovalIndex.getStateById().get(stateRef);

        if (state == null) {
            throw new IllegalArgumentException("OVAL state not found: " + stateRef);
        }

        return state;
    }
}
