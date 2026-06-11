package com.touchstone.compiler.content;

public interface ContentPackageLoader {
    ContentPackage load(String contentPackageId, String contentVersion);
}
