package com.touchstone.compiler.oval.windows.userright;

import com.touchstone.compiler.model.parsed.oval.ParsedOvalObject;
import com.touchstone.compiler.oval.CompiledObjectPlan;
import com.touchstone.compiler.oval.EntitySelector;
import com.touchstone.compiler.oval.ObjectCompilationResult;
import com.touchstone.compiler.oval.OvalCheckCompileContext;
import com.touchstone.compiler.oval.common.CheckCompilerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User Rights Assignment checks ("Deny log on locally", ...). The object
 * names a user right constant (e.g. SE_NETWORK_LOGON_NAME); the agent
 * collects the trustees holding it (one item per trustee), and states assert
 * trustee_sid/trustee_name. "Set to No One" rules carry no state and use
 * check_existence="none_exist".
 */
@Component
public class UserRightCheckCompiler extends CheckCompilerBase {

    @Override
    protected String supportedTestType() {
        return "userright_test";
    }

    @Override
    protected ObjectCompilationResult compileSimpleObject(
            final OvalCheckCompileContext context,
            final ParsedOvalObject object) {
        final EntitySelector userright = object.findEntity("userright")
                .orElseThrow(() -> new IllegalArgumentException("Missing userright"))
                .resolve();

        final UserRightCollectionTask task = new UserRightCollectionTask();
        task.addSelector(userright);

        return new ObjectCompilationResult(
                object.getObjectId(),
                CompiledObjectPlan.builder()
                        .objectId(object.getObjectId())
                        .objectType(object.getObjectType())
                        .tasks(new ArrayList<>(List.of(task)))
                        .build());
    }
}
