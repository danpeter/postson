package com.danpeter.postson;

import com.danpeter.postson.annotations.Id;
import com.danpeter.postson.annotations.Table;

@Table(name = "book")
public class BookWithIsbn {

    @Id
    private final String isbn;
    private final String author;

    public BookWithIsbn(String isbn, String author) {
        this.isbn = isbn;
        this.author = author;
    }

    public String isbn() {
        return isbn;
    }
}
