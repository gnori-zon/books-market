package org.gnori.booksmarket.feauture.genre;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.gnori.booksmarket.core.EntityType;
import org.gnori.booksmarket.core.exception.AppException;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository repository;

    public Genre getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFoundGenreById(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Genre create(Genre.Create create) {
        create.validate();
        validateUniquenessName(create.name());
        repository.create(create);
        return create.toGenre();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Genre updateById(UUID id, Genre.Update update) {
        update.validate();
        final var exists = repository.findById(id).orElseThrow(() -> notFoundGenreById(id));
        if (!exists.hasDifferences(update)) {
            return exists;
        }
        if (update.contains(Genre.Update.Item.Name.class)) {
            final var updateItem = update.get(Genre.Update.Item.Name.class);
            if (updateItem.hasDifference(exists)) {
                validateUniquenessName(updateItem.value());
            }
        }
        repository.updateById(id, update);
        return exists.apply(update);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(UUID id) {
        final boolean isDeleted = repository.deleteById(id);
        if (!isDeleted) {
            throw notFoundGenreById(id);
        }
    }

    private void validateUniquenessName(String newName) {
        if (repository.findByName(newName).isPresent()) {
            throw BaseAppException.uniqueConstraintViolation("name");
        }
    }

    public Collection<Genre> listByIds(@NonNull Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Genre> genres = repository.findByIdIn(ids);
        final Map<UUID, Genre> result = new HashMap<>(ids.size());
        genres.forEach(genre -> result.put(genre.id(), genre));
        if (ids.size() == result.size()) {
            return result.values();
        }
        throw SetUtils.difference(ids, result.keySet()).stream()
            .map(GenreService::notFoundGenreById)
            .collect(BaseAppException.toListCollector());
    }

    private static AppException notFoundGenreById(UUID id) {
        return BaseAppException.notFoundById(EntityType.GENRE, id);
    }
}
