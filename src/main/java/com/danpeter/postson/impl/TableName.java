package com.danpeter.postson.impl;

import com.danpeter.postson.annotations.Table;
import com.google.common.base.CaseFormat;

import java.util.Optional;

public class TableName {
    private final String tableName;

    private TableName(String tableName) {
        this.tableName = tableName;
    }

    public static <T> TableName from(Class<T> clazz) {
        String tableName = Optional.ofNullable(clazz.getAnnotation(Table.class))
                .map(Table::name)
                .orElse(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()));
        return new TableName(tableName);
    }

    @Override
    public String toString() {
        return tableName;
    }
}
