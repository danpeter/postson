package com.danpeter.postson;

public class DatastoreException extends RuntimeException {
    public DatastoreException(Exception e) {
        super(e);
    }

    public DatastoreException(String s) {
        super(s);
    }
}
