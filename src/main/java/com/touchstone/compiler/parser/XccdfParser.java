package com.touchstone.compiler.parser;

import com.touchstone.compiler.model.parsed.xccdf.ParsedXccdfBenchmark;

import java.io.InputStream;

public interface XccdfParser {
    ParsedXccdfBenchmark parse(InputStream inputStream);
}
