package com.danpeter.postson.impl;

import com.danpeter.postson.ObjectQuery;
import com.danpeter.postson.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JsonObjectQuery<T> implements ObjectQuery<T> {
    private final Class<T> type;
    private final QueryExecutor queryExecutor;
    private final Gson gson;
    private final JsonObject queryObject = new JsonObject();

    public JsonObjectQuery(Class<T> type, QueryExecutor queryExecutor, Gson gson) {
        this.type = type;
        this.queryExecutor = queryExecutor;
        this.gson = gson;
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
        return queryExecutor.asList(type, "data @> ?::JSONB", Arrays.asList(queryObject.toString()));
    }

    @Override
    public Optional<T> singleResult() {
        return queryExecutor.singleResult(type, "data @> ?::JSONB", Arrays.asList(queryObject.toString()));
    }

    @Override
    public int count() {
        return queryExecutor.count(type, "data @> ?::JSONB", Arrays.asList(queryObject.toString()));
    }

    @Override
    public int delete() {
        return queryExecutor.delete(type, "data @> ?::JSONB", Arrays.asList(queryObject.toString()));
    }
}
