package com.touchstone.compiler.content;

import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class LocalContentPackageLoader implements ContentPackageLoader {

    @Override
    public ContentPackage load(final String contentPackageId, final String contentVersion) {
        final ClassLoader classLoader = getClass().getClassLoader();

        final InputStream xccdfStream =
                classLoader.getResourceAsStream("content/" + contentPackageId + "/" + contentVersion + "/xccdf.xml");

        final InputStream ovalStream =
                classLoader.getResourceAsStream("content/" + contentPackageId + "/" + contentVersion + "/oval.xml");

        if (xccdfStream == null) {
            throw new IllegalArgumentException("XCCDF content not found for package: "
                    + contentPackageId + ", version: " + contentVersion);
        }

        if (ovalStream == null) {
            throw new IllegalArgumentException("OVAL content not found for package: "
                    + contentPackageId + ", version: " + contentVersion);
        }

        return new ContentPackage(xccdfStream, ovalStream);
    }
}
