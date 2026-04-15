package com.example.scap.port;

public interface ContentStoragePort {
    String put(String key, byte[] bytes, String contentType);
    byte[] get(String key);
}