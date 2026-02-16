package org.gnori.booksmarket.storage.repository;

import java.util.UUID;

public interface AuthorRepository {
    void create(Author.Create create);
    boolean updateById(UUID id, Author.Update update);

    boolean deleteById(UUID id);
}
