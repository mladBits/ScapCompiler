package com.touchstone.compiler.content;

public interface ContentPackageLoader {

    /**
     * Loads a content package by its storage key (the package directory name
     * in the raw-content bucket, e.g. "CIS_Microsoft_Windows_11_Enterprise_Benchmark_v5.0.1").
     * Content versions are embedded in package names.
     */
    ContentPackage load(String packageId);
}
