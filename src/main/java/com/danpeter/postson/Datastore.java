package com.danpeter.postson;

import java.util.Optional;

public interface Datastore {

    <T> void save(T entity);

    <T> Query<T> createObjectQuery(Class<T> type);

    <T, V> Optional<T> get(Class<T> type, V id);

    <T, V> boolean delete(Class<T> type, V id);
}
