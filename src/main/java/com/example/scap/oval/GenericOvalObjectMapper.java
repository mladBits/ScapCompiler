package com.example.scap.oval;

import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.normalize.OvalEntityNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenericOvalObjectMapper {

    private final OvalEntityNormalizer entityNormalizer;

    public <T extends CompiledOvalObjectPlan> T map(final ParsedOvalObject source, final T target) {
        target.setObjectId(source.getObjectId());
        target.setObjectType(source.getObjectType());
        target.setNamespace(source.getNamespace());

        for (final ParsedOvalEntity entity : source.getEntities()) {
            target.getEntities().add(entityNormalizer.normalize(entity));
        }

        return target;
    }
}
