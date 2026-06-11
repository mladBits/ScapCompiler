package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.compiled.variables.LocalVariableCompilationResult;
import com.example.scap.model.parsed.oval.ParsedOvalEntity;
import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import com.example.scap.oval.windows.registry.RegistryCheckCompiler;
import com.example.scap.variables.ResolvedVariableBindings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OvalCheckCompilationServiceImplTest {

    private final OvalCheckCompilationServiceImpl service =
            new OvalCheckCompilationServiceImpl(List.of(new RegistryCheckCompiler()));

    @Test
    void compile_shouldCompileVariableReferencedObjectsWithoutATest() {
        OvalIndex index = new OvalIndex();
        index.getObjectById().put("oval:t:obj:99", registryObject("oval:t:obj:99"));

        OvalCheckCompilationResult result = service.compile(
                index,
                emptySlice(),
                new ResolvedVariableBindings(),
                new LocalVariableCompilationResult(),
                List.of("oval:t:obj:99"));

        assertTrue(result.getObjects().containsKey("oval:t:obj:99"));
        assertTrue(result.getFailedObjectIds().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void compile_shouldRecordMissingVariableReferencedObjects() {
        OvalCheckCompilationResult result = service.compile(
                new OvalIndex(),
                emptySlice(),
                new ResolvedVariableBindings(),
                new LocalVariableCompilationResult(),
                List.of("oval:t:obj:404"));

        assertTrue(result.getFailedObjectIds().contains("oval:t:obj:404"));
        assertTrue(result.getWarnings().getFirst().contains("oval:t:obj:404"));
    }

    @Test
    void compile_shouldRecordObjectsWithoutACompiler() {
        ParsedOvalObject object = new ParsedOvalObject();
        object.setObjectId("oval:t:obj:7");
        object.setObjectType("unknownprobe_object");

        OvalIndex index = new OvalIndex();
        index.getObjectById().put("oval:t:obj:7", object);

        OvalCheckCompilationResult result = service.compile(
                index,
                emptySlice(),
                new ResolvedVariableBindings(),
                new LocalVariableCompilationResult(),
                List.of("oval:t:obj:7"));

        assertTrue(result.getFailedObjectIds().contains("oval:t:obj:7"));
        assertTrue(result.getWarnings().getFirst().contains("unknownprobe_object"));
    }

    private ResolvedOvalEvaluationSlice emptySlice() {
        return new ResolvedOvalEvaluationSlice(List.of(), List.of(), List.of(), List.of());
    }

    private ParsedOvalObject registryObject(final String objectId) {
        ParsedOvalObject object = new ParsedOvalObject();
        object.setObjectId(objectId);
        object.setObjectType("registry_object");
        object.getEntities().add(entity("hive", "HKEY_LOCAL_MACHINE"));
        object.getEntities().add(entity("key", "SOFTWARE\\Test"));
        object.getEntities().add(entity("name", "Value"));
        return object;
    }

    private ParsedOvalEntity entity(final String name, final String value) {
        ParsedOvalEntity entity = new ParsedOvalEntity();
        entity.setName(name);
        entity.setValue(value);
        return entity;
    }
}