package com.touchstone.compiler.content;

import java.io.InputStream;

public record ContentPackage(
        InputStream xccdfStream,
        InputStream ovalStream
) {
}
