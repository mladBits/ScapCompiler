package com.touchstone.compiler.variables;

public enum VariableBindingSource {
    USER_SUPPLIED,
    XCCDF_VALUE,
    XCCDF_DEFAULT,
    OVAL_CONSTANT,
    RUNTIME_LOCAL
}
