package com.example.scap.index;

import com.example.scap.model.parsed.oval.ParsedOval;
import org.springframework.stereotype.Component;

@Component
public class OvalIndexBuilder {
    public OvalIndex build(final ParsedOval oval) {
        final OvalIndex index = new OvalIndex();
        oval.getDefinitions().forEach(definition -> index.getDefinitionById().put(definition.getId(), definition));
        oval.getTests().forEach(test -> index.getTestById().put(test.getId(), test));
        oval.getObjects().forEach(object -> index.getObjectById().put(object.getObjectId(), object));
        oval.getStates().forEach(state -> index.getStateById().put(state.getStateId(), state));
        return index;
    }
}
