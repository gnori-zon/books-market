package org.gnori.booksmarket.feauture.genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;

public interface GenreRepository {
    Optional<Genre> findById(UUID id);

    void create(Genre.Create create);

    boolean updateById(UUID id, Genre.Update update);

    boolean deleteById(UUID id);

    Optional<Object> findByName(String name);

    List<Genre> findByIdIn(@NonNull Set<UUID> ids);
}
