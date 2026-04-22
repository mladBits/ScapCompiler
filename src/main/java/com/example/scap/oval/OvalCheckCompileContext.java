package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;
import lombok.Getter;

@Getter
public class OvalCheckCompileContext {
    private final OvalIndex ovalIndex;
    private final ResolvedOvalEvaluationSlice slice;

    public OvalCheckCompileContext(
            final OvalIndex ovalIndex,
            final ResolvedOvalEvaluationSlice slice
    ) {
        this.ovalIndex = ovalIndex;
        this.slice = slice;
    }

}
