package com.example.scap.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalState;
import com.example.scap.normalize.OvalEntityNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenericOvalStateMapper {

    private final OvalEntityNormalizer entityNormalizer;

    public <T extends CompiledOvalStatePlan> T map(final ParsedOvalState source, final T target) {
        target.setStateId(source.getStateId());
        target.setStateType(source.getStateType());
        target.setNamespace(source.getNamespace());

        for (final ParsedOvalEntity entity : source.getEntities()) {
            target.getEntities().add(entityNormalizer.normalize(entity));
        }

        return target;
    }
}
