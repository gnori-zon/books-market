package org.gnori.booksmarket.feauture.author;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;

public interface AuthorRepository {
    Optional<Author> findById(UUID id);

    void create(Author.Create create);

    boolean updateById(UUID id, Author.Update update);

    boolean deleteById(UUID id);

    List<Author> findByIdIn(@NonNull Set<UUID> ids);
}
