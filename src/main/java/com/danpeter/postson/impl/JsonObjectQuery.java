package com.danpeter.postson.impl;

import com.danpeter.postson.DatastoreException;
import com.danpeter.postson.Query;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonObjectQuery<T> implements Query<T> {
    private final Class<T> type;
    private final DataSource dataSource;
    private final Gson gson;
    private final JsonObject queryObject = new JsonObject();
    private final String tableName;

    protected JsonObjectQuery(Class<T> type, DataSource dataSource, Gson gson) {
        this.type = type;
        this.dataSource = dataSource;
        this.gson = gson;
        this.tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getSimpleName());
    }

    @Override
    public <V> Query<T> field(String field, V value) {
        addQuery(JsonObjectQuery.this.queryObject, Arrays.asList(field.split("\\.")), value);
        return this;
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
}
