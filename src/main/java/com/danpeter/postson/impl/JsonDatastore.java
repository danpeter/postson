package com.danpeter.postson.impl;


import com.danpeter.postson.Datastore;
import com.danpeter.postson.ObjectQuery;
import com.danpeter.postson.PrimitiveQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

/**
 * Datastore used for storing object as JSONB in Postgres.
 */
public class JsonDatastore implements Datastore {
    private final Gson gson;
    private final QueryExecutor queryExecutor;

    /**
     * Constructs a JsonDatastore
     *
     * @param dataSource The JDBC datasource.
     */
    public JsonDatastore(DataSource dataSource) {
        gson = new Gson();
        queryExecutor = new QueryExecutor(dataSource, gson);
    }

    /**
     * Constructs a JsonDatastore using adapters, adds support for serializing and de-serializing certain classes.
     *
     * @param dataSource The JDBC datasource.
     * @param adapters   A map of classes and their adapters.
     */
    public JsonDatastore(DataSource dataSource, Map<Class, Object> adapters) {
        GsonBuilder builder = new GsonBuilder();
        adapters.forEach(builder::registerTypeAdapter);
        gson = builder.create();
        queryExecutor = new QueryExecutor(dataSource, gson);
    }

    @Override
    public <T> void save(final T entity) {
        queryExecutor.save(entity);
    }

    @Override
    public <T, V> Optional<T> get(Class<T> type, V id) {
        final String idField = IdField.from(type).toString();
        return createObjectQuery(type).field(idField, id).singleResult();
    }

    @Override
    public <T, V> boolean delete(Class<T> type, V id) {
        final String idField = IdField.from(type).toString();
        return createObjectQuery(type).field(idField, id).delete() > 0;
    }

    @Override
    public <T> ObjectQuery<T> createObjectQuery(Class<T> type) {
        return new JsonObjectQuery<>(type, queryExecutor, gson);
    }

    @Override
    public <T> PrimitiveQuery<T> createPrimitiveQuery(Class<T> type) {
        return new JsonPrimitiveQuery<>(type, queryExecutor);
    }
}

