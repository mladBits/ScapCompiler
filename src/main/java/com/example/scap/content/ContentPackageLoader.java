package com.example.scap.content;

public interface ContentPackageLoader {
    ContentPackage load(String contentPackageId, String contentVersion);
}
