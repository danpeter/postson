package com.danpeter.postson.impl;

import com.danpeter.postson.DatastoreException;
import com.danpeter.postson.FieldQuery;
import com.danpeter.postson.PrimitiveQuery;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonPrimitiveQuery<T> implements PrimitiveQuery<T> {

    private final Class<T> type;
    private final DataSource dataSource;
    private final Gson gson;
    private final String tableName;
    private Collection<String> queryFilters = new ArrayList<>();

    protected JsonPrimitiveQuery(Class<T> type, DataSource dataSource, Gson gson) {
        this.type = type;
        this.dataSource = dataSource;
        this.gson = gson;
        this.tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, type.getSimpleName());
    }

    @Override
    public JsonFieldQuery field(String field) {
        return new JsonFieldQuery(field);
    }

    @Override
    public List<T> asList() {
        String query = queryFilters.stream().collect(Collectors.joining("AND", "SELECT id, data FROM " + tableName + " WHERE ", ""));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            //TODO: Handle parameters in the prepared statement
//            preparedStatement.setString(1, queryObject.toString());
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
        List<T> result = asList();
        if (result.size() > 1) {
            throw new DatastoreException("No unique result.");
        }
        return result.stream().findAny();
    }

    @Override
    public int count() {
        String query = queryFilters.stream().collect(Collectors.joining("AND", "SELECT COUNT(*) AS total FROM " + tableName + " WHERE ", ""));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            //TODO: Handle parameters in the prepared statement
//            preparedStatement.setString(1, queryObject.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt("total");
        } catch (SQLException e) {
            throw new DatastoreException(e);
        }
    }

    @Override
    public int delete() {
        String query = queryFilters.stream().collect(Collectors.joining("AND", "DELETE FROM " + tableName + " WHERE ", ""));

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            //TODO: Handle parameters in the prepared statement
//            preparedStatement.setString(1, queryObject.toString());
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
        public <V> PrimitiveQuery<T> equal(V value) {
            queryFilters.add(jsonPath(field) + " = '" + value + "'");
            return JsonPrimitiveQuery.this;
        }

        @Override
        public <V> PrimitiveQuery<T> greaterThan(int value) {
            queryFilters.add(jsonPath(field) + " > '" + value + "'");
            return JsonPrimitiveQuery.this;
        }

        @Override
        public <V> PrimitiveQuery<T> lessThan(int value) {
            queryFilters.add(jsonPath(field) + " < '" + value + "'");
            return JsonPrimitiveQuery.this;
        }

        @Override
        public PrimitiveQuery<T> beginsWith(String beginning) {
            queryFilters.add(jsonPath(field) + " like '" + beginning + "%'");
            return JsonPrimitiveQuery.this;
        }

        @Override
        public PrimitiveQuery<T> beginsWithIgnoreCase(String beginning) {
            queryFilters.add(jsonPath(field) + " ilike '" + beginning + "%'");
            return JsonPrimitiveQuery.this;
        }

        private String jsonPath(String field) {
            String[] args = field.split("\\.");
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append("data");
            for (int i = 0; i < args.length; i++) {
                if (i == args.length - 1) {
                    pathBuilder.append(" ->> ");
                } else {
                    pathBuilder.append(" -> ");
                }
                pathBuilder.append("'" + args[i] + "'");
            }
            return pathBuilder.toString();
        }
    }
}

