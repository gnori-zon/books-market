package org.gnori.booksmarket.feauture.book.core;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository {
    Optional<Book> findById(UUID id);
    Optional<Book> findByIdForUpdate(UUID id);
    void create(Book.Create create);
    boolean updateById(UUID id, Book.Update update);
    boolean deleteById(UUID id);
}
