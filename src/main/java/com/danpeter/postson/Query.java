package com.danpeter.postson;

import java.util.List;
import java.util.Optional;

public interface Query<T> {
    FieldQuery<T> field(String field);

    List<T> asList();

    Optional<T> singleResult();

    int count();

    int delete();
}
