package com.danpeter.postson.impl;

import com.danpeter.postson.FieldQuery;
import com.danpeter.postson.PrimitiveQuery;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class JsonPrimitiveQuery<T> implements PrimitiveQuery<T> {

    private final Class<T> type;
    private final QueryExecutor queryExecutor;
    private final Collection<String> queryFilters = new ArrayList<>();

    public JsonPrimitiveQuery(Class<T> type, QueryExecutor queryExecutor) {
        this.type = type;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public JsonFieldQuery field(String field) {
        return new JsonFieldQuery(field);
    }

    @Override
    public List<T> asList() {
        return queryExecutor.asList(type, createFromStatement(), emptyList());
    }

    @Override
    public Optional<T> singleResult() {
        return queryExecutor.singleResult(type, createFromStatement(), emptyList());
    }

    private String createFromStatement() {
        return queryFilters.stream().collect(Collectors.joining(" AND "));
    }

    @Override
    public int count() {
        return queryExecutor.count(type, createFromStatement(), emptyList());
    }

    @Override
    public int delete() {
        return queryExecutor.delete(type, createFromStatement(), emptyList());
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
        public PrimitiveQuery<T> greaterThan(int value) {
            queryFilters.add(jsonPath(field) + " > '" + value + "'");
            return JsonPrimitiveQuery.this;
        }

        @Override
        public PrimitiveQuery<T> lessThan(int value) {
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

