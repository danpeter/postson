package com.danpeter.postson;

public interface FieldQuery<T> {
    <V> PrimitiveQuery<T> equal(V value);

    PrimitiveQuery<T> greaterThan(int value);

    PrimitiveQuery<T> lessThan(int value);

    PrimitiveQuery<T> beginsWith(String beginning);

    PrimitiveQuery<T> beginsWithIgnoreCase(String beginning);
}
