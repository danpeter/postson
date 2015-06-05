package com.danpeter.postson.impl;

import com.danpeter.postson.annotations.Id;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

public class IdField {

    public static final String ID = "id";
    private static Predicate<Field> hasIdAnnotation = (field) -> Optional.ofNullable(field.getAnnotation(Id.class)).isPresent();
    private final String idField;

    public IdField(String idField) {
        this.idField = idField;
    }

    public static <T> IdField from(Class<T> clazz) {
        //TODO: Check that there is only one id annotation
        String idField = stream(clazz.getDeclaredFields())
                .filter(hasIdAnnotation)
                .findAny()
                .map(Field::getName)
                .orElse(ID);
        return new IdField(idField);
    }

    @Override
    public String toString() {
        return idField;
    }
}
