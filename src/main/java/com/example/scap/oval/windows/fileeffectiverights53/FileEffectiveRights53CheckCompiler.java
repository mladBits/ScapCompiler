package com.example.scap.oval.windows.fileeffectiverights53;

import com.example.scap.model.parsed.oval.ParsedOvalObject;
import com.example.scap.oval.CompiledObjectPlan;
import com.example.scap.oval.ObjectCompilationResult;
import com.example.scap.oval.OvalCheckCompileContext;
import com.example.scap.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileEffectiveRights53CheckCompiler extends CheckCompilerBase {
    @Override
    protected String supportedTestType() {
        return "fileeffectiverights53_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final FileEffectiveRights53CollectionTask task = new FileEffectiveRights53CollectionTask();

        // The object addresses the file either by filepath, or by path + filename.
        object.findEntity("filepath").ifPresent(entity -> task.addSelector(entity.resolve()));
        object.findEntity("path").ifPresent(entity -> task.addSelector(entity.resolve()));
        object.findEntity("filename").ifPresent(entity -> task.addSelector(entity.resolve()));

        if (task.getSelectors().isEmpty()) {
            throw new IllegalArgumentException("Missing filepath or path/filename");
        }

        task.addSelector(object.findEntity("trustee_sid")
                .orElseThrow(() -> new IllegalArgumentException("Missing trustee_sid"))
                .resolve());

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
