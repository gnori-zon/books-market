package org.gnori.booksmarket.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum EntityType {
    AUTHOR("author"),
    PUBLISHER("publisher"),
    GENRE("genre"),
    BOOK("book");

    private final String value;
}
