package com.danpeter.postson;


import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JsonDatastore {
    final Connection connection;
    final Gson gson;

    public JsonDatastore(Connection connection) {
        this.connection = connection;
        this.gson = new Gson();
    }

    public <T> void save(final T entity) {
        String json = gson.toJson(entity);

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO system_user (data) VALUES (?::JSONB)")) {
            preparedStatement.setString(1, json);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new JsonDatastoreException(e);
        }
    }

    public <T> Query<T> createQuery(Class<T> type) {
        return new Query<>(type);
    }

    public class Query<T> {
        private final Class<T> type;
        private StringBuilder query = new StringBuilder("SELECT id, data FROM ");
        private JsonObject queryObject = new JsonObject();

        public Query(Class<T> type) {
            this.type = type;
            String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getName());
            query.append(tableName);
        }

        public FieldQuery field(String field) {
            return new FieldQuery(field);
        }

        public List<T> asList() {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query + " WHERE data @> ?::JSONB")) {
                preparedStatement.setString(1, queryObject.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(gson.fromJson(resultSet.getString("data"), type));
                }
                return result;
            } catch (SQLException e) {
                throw new JsonDatastoreException(e);
            }
        }

        public class FieldQuery {
            final String field;

            public FieldQuery(String field) {
                this.field = field;
            }

            public Query<T> equal(String value) {
                addQuery(Query.this.queryObject, Arrays.asList(this.field.split("\\.")), value);
                return Query.this;
            }

            private void addQuery(JsonObject queryObject, List<String> fields, String value) {
                if (fields.size() == 1) {
                    queryObject.addProperty(fields.get(0), value);
                } else {
                    JsonObject nextQueryObject = new JsonObject();
                    addQuery(nextQueryObject, fields.subList(1, fields.size()), value);
                    queryObject.add(fields.get(0), nextQueryObject);
                }
            }

        }
    }
}

