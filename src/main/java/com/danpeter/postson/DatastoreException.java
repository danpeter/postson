package com.danpeter.postson;

import java.sql.SQLException;

public class DatastoreException extends RuntimeException {
    public DatastoreException(Exception e) {
        super(e);
    }

    public DatastoreException(String s) {
        super(s);
    }

    public DatastoreException(String s, SQLException e) {
        super(s, e);
    }
}
