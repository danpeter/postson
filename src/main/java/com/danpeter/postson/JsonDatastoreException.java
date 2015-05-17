package com.danpeter.postson;

public class JsonDatastoreException extends RuntimeException {
    public JsonDatastoreException(Exception e) {
        super(e);
    }

    public JsonDatastoreException(String s) {
        super(s);
    }
}
