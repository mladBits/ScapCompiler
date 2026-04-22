package com.example.scap.content;

import java.io.InputStream;

public record ContentPackage(
        InputStream xccdfStream,
        InputStream ovalStream
) {
}
