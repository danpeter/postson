package com.danpeter.postson.impl;

import com.danpeter.postson.DatastoreException;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class QueryExecutor {
    private final DataSource dataSource;
    private final Gson gson;

    QueryExecutor(DataSource dataSource, Gson gson) {
        this.dataSource = dataSource;
        this.gson = gson;
    }

    <T> void save(T entity) {
        final String tableName = TableName.from(entity.getClass()).toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (data) VALUES (?::JSONB)")) {
            preparedStatement.setString(1, gson.toJson(entity));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatastoreException(e);
        }
    }

    <T> List<T> asList(Class<T> type, String fromStatement, List<Object> parameters) {
        final String tableName = TableName.from(type).toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, data FROM " + tableName + " WHERE " + fromStatement)) {
            setParameters(parameters, preparedStatement);
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

    <T> Optional<T> singleResult(Class<T> type, String fromStatement, List<Object> parameters) {
        List<T> result = asList(type, fromStatement, parameters);
        if (result.size() > 1) {
            throw new DatastoreException("No unique result.");
        }
        return result.stream().findAny();
    }

    <T> int count(Class<T> type, String fromStatement, List<Object> parameters) {
        final String tableName = TableName.from(type).toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS total FROM " + tableName + " WHERE " + fromStatement)) {
            setParameters(parameters, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("total");
        } catch (SQLException e) {
            throw new DatastoreException(e);
        }
    }

    <T> int delete(Class<T> type, String fromStatement, List<Object> parameters) {
        final String tableName = TableName.from(type).toString();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + fromStatement)) {
            setParameters(parameters, preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DatastoreException(e);
        }
    }

    private void setParameters(List<Object> parameters, PreparedStatement preparedStatement) throws SQLException {
        int count = 0;
        for(Object param : parameters) {
            preparedStatement.setObject(++count, param);
        }
    }
}
