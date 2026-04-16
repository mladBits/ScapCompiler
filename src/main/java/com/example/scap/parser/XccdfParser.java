package com.example.scap.parser;

import com.example.scap.model.parsed.xccdf.ParsedXccdfBenchmark;

import java.io.InputStream;

public interface XccdfParser {
    ParsedXccdfBenchmark parse(InputStream inputStream);
}
