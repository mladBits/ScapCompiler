package com.touchstone.compiler.model.parsed.oval;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ParsedOvalObject extends ParsedOvalObjectBase {
    private final List<ParsedOvalEntity> entities = new ArrayList<>();

    public Optional<ParsedOvalEntity> findEntity(final String entityName) {
        return entities.stream()
                .filter(entity -> entityName.equals(entity.getName()))
                .findFirst();
    }
}
