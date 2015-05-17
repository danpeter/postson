package com.danpeter.postson;

public interface FieldQuery<T> {
    <V> Query<T> equal(V value);
}
