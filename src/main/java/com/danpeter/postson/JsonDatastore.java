package com.danpeter.postson;


import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
     * @param adapters A map of classes and their adapters.
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
    public <T> Query<T> createQuery(Class<T> type) {
        return new JsonQuery<>(type);
    }

    @Override
    public <T, V> Optional<T> get(Class<T> type, V id) {
        return createQuery(type).field("id").equal(id).singleResult();
    }

    @Override
    public <T, V> boolean delete(Class<T> type, V id) {
        return createQuery(type).field("id").equal(id).delete() > 0;
    }

    public class JsonQuery<T> implements Query<T> {
        private final Class<T> type;
        private final JsonObject queryObject = new JsonObject();
        private final String tableName;

        public JsonQuery(Class<T> type) {
            this.type = type;
            this.tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getSimpleName());
        }

        @Override
        public FieldQuery<T> field(String field) {
            return new JsonFieldQuery(field);
        }

        @Override
        public List<T> asList() {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, data FROM " + tableName + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(gson.fromJson(resultSet.getString("data"), type));
                }
                return result;
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        }

        @Override
        public Optional<T> singleResult() {
            //TODO: Optimize by doing count first, fail if greater than 1
            List<T> result = asList();
            if (result.size() > 1) {
                throw new DatastoreException("No unique result.");
            }
            return result.stream().findAny();
        }

        @Override
        public int count() {

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS total FROM " + tableName + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getInt("total");
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        }

        @Override
        public int delete() {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                return preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        }

        public class JsonFieldQuery implements FieldQuery<T> {
            final String field;

            public JsonFieldQuery(String field) {
                this.field = field;
            }

            @Override
            public <V> Query<T> equal(V value) {
                addQuery(JsonQuery.this.queryObject, Arrays.asList(this.field.split("\\.")), value);
                return JsonQuery.this;
            }

            private <V> void addQuery(JsonObject queryObject, List<String> fields, V value) {
                if (fields.size() == 1) {
                    queryObject.add(fields.get(0), gson.toJsonTree(value));
                } else {
                    JsonObject nextQueryObject = new JsonObject();
                    addQuery(nextQueryObject, fields.subList(1, fields.size()), value);
                    queryObject.add(fields.get(0), gson.toJsonTree(nextQueryObject));
                }
            }
        }
    }
}

