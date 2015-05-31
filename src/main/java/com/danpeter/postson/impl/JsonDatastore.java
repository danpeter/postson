package com.danpeter.postson.impl;


import com.danpeter.postson.Datastore;
import com.danpeter.postson.DatastoreException;
import com.danpeter.postson.Query;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Datastore used for storing object as JSONB in Postgres.
 */
public class JsonDatastore implements Datastore {
    private final DataSource dataSource;
    private final Gson gson;

    /**
     * Constructs a JsonDatastore
     *
     * @param dataSource The JDBC datasource.
     */
    public JsonDatastore(DataSource dataSource) {
        this.dataSource = dataSource;
        this.gson = new Gson();
    }

    /**
     * Constructs a JsonDatastore using adapters, adds support for serializing and de-serializing certain classes.
     *
     * @param dataSource The JDBC datasource.
     * @param adapters   A map of classes and their adapters.
     */
    public JsonDatastore(DataSource dataSource, Map<Class, Object> adapters) {
        this.dataSource = dataSource;
        GsonBuilder builder = new GsonBuilder();
        adapters.forEach(builder::registerTypeAdapter);
        this.gson = builder.create();
    }

    @Override
    public <T> void save(final T entity) {
        try (Connection connection = dataSource.getConnection()) {
            String json = gson.toJson(entity);
            String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getClass().getSimpleName());

            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (data) VALUES (?::JSONB)")) {
                preparedStatement.setString(1, json);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T, V> Optional<T> get(Class<T> type, V id) {
        return createObjectQuery(type).field("id", id).singleResult();
    }

    @Override
    public <T, V> boolean delete(Class<T> type, V id) {
        return createObjectQuery(type).field("id", id).delete() > 0;
    }

    @Override
    public <T> Query<T> createObjectQuery(Class<T> type) {
        return new JsonObjectQuery<>(type, dataSource, gson);
    }
}

