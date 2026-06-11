package com.touchstone.compiler.resolve.xccdf;

import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.touchstone.compiler.model.resolved.xccdf.ResolvedProfile;

public interface ProfileResolver {
    ResolvedProfile resolve(ParsedXccdfBenchmark benchmark, String profileId);
}
