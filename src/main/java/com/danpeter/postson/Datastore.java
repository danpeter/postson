package com.danpeter.postson;

import java.util.Optional;

public interface Datastore {

    <T> void save(T entity);

    <T> JdbcDatastore.Query<T> createQuery(Class<T> type);

    <T> Optional<T> get(Class<T> type, String id);
}
