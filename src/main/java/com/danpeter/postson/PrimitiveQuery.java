package com.danpeter.postson;

public interface PrimitiveQuery<T> extends Query<T> {
    FieldQuery<T> field(String field);
}
