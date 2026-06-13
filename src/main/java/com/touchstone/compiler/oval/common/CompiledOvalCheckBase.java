package com.touchstone.compiler.oval.common;

import com.touchstone.compiler.oval.CompiledOvalCheck;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompiledOvalCheckBase implements CompiledOvalCheck  {
    private String testId;
    private String objectId;
    private List<String> stateIds;
    private String check;
    private String checkExistence;

    /**
     * How results of multiple states fold for one item (OVAL state_operator):
     * AND, OR, ONE, XOR. Always present; AND when the source omits it.
     */
    private String stateOperator;
}
