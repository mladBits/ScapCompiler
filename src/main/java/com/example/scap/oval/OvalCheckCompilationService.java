package com.example.scap.oval;

import com.example.scap.index.OvalIndex;
import com.example.scap.model.resolved.oval.ResolvedOvalEvaluationSlice;

public interface OvalCheckCompilationService {
    OvalCheckCompilationResult compile(
            OvalIndex ovalIndex,
            ResolvedOvalEvaluationSlice slice
    );
}
