package com.touchstone.compiler.store;

import com.touchstone.compiler.model.compiled.ExecutionTemplate;

public interface ExecutionTemplateStore {

    /**
     * Persists the template and returns its artifact location.
     */
    String store(ExecutionTemplate template);
}
