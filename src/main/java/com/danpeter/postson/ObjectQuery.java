package com.danpeter.postson;

public interface ObjectQuery<T> extends Query<T> {
    <V> Query<T> field(String field, V value);
}
