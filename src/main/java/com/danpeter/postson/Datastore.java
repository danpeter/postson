package com.danpeter.postson;

public interface Datastore {

    <T> void save(T entity);

    <T> JsonDatastore.Query<T> createQuery(Class<T> type);
}
