package com.danpeter.postson;


import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.*;

public class JdbcDatastore implements Datastore {
    final Connection connection;
    final Gson gson;

    public JdbcDatastore(String host, String port, String databaseName, String userName, String password) {
        try {
            this.connection = DriverManager.getConnection(
                    String.format("jdbc:postgresql://%s:%s/%s", host, port, databaseName), userName, password);
        } catch (SQLException e) {
            throw new DatastoreException("Could not connect to underlying data source.", e);
        }
        this.gson = new Gson();
    }

    @Override
    public <T> void save(final T entity) {
        String json = gson.toJson(entity);
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getClass().getSimpleName());

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (data) VALUES (?::JSONB)")) {
            preparedStatement.setString(1, json);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatastoreException(e);
        }
    }

    @Override
    public <T> Query<T> createQuery(Class<T> type) {
        return new Query<>(type);
    }

    @Override
    public <T, V> Optional<T> get(Class<T> type, V id) {
        return createQuery(type).field("id").equal(id).singleResult();
    }

    @Override
    public <T, V> boolean delete(Class<T> type, V id) {
        return createQuery(type).field("id").equal(id).delete() > 0;
    }

    public class Query<T> {
        private final Class<T> type;
        private JsonObject queryObject = new JsonObject();
        private String tableName;

        public Query(Class<T> type) {
            this.type = type;
            this.tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getSimpleName());
        }

        public FieldQuery field(String field) {
            return new FieldQuery(field);
        }

        public List<T> asList() {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, data FROM " + tableName + " WHERE data @> ?::JSONB")) {
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

        public Optional<T> singleResult() {
            //TODO: Optimize by doing count first, fail if greater than 1
            List<T> result = asList();
            if (result.size() > 1) {
                throw new DatastoreException("No unique result.");
            }
            return result.stream().findAny();
        }

        public int count() {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS total FROM " + tableName + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getInt("total");
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        }

        public int delete() {
            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                return preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DatastoreException(e);
            }
        }

        public class FieldQuery {
            final String field;

            public FieldQuery(String field) {
                this.field = field;
            }

            public <V> Query<T> equal(V value) {
                addQuery(Query.this.queryObject, Arrays.asList(this.field.split("\\.")), value);
                return Query.this;
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

