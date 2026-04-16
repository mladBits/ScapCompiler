package com.example.scap.resolve.xccdf;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;
import com.example.scap.model.resolved.xccdf.ResolvedProfile;

public interface ProfileResolver {
    ResolvedProfile resolve(ParsedXccdfBenchmark benchmark, String profileId);
}
