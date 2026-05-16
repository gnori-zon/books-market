package org.gnori.booksmarket.feauture.author;

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
public class AuthorService {
    private final AuthorRepository repository;

    public Author getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFoundAuthorById(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Author create(Author.Create create) {
        create.validate();
        repository.create(create);
        return create.toAuthor();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Author updateById(UUID id, Author.Update update) {
        update.validate();
        final var exists = repository.findById(id).orElseThrow(() -> notFoundAuthorById(id));
        if (!exists.hasDifferences(update)) {
            return exists;
        }
        repository.updateById(id, update);
        return exists.apply(update);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(UUID id) {
        final boolean isDeleted = repository.deleteById(id);
        if (!isDeleted) {
            throw notFoundAuthorById(id);
        }
    }

    public Collection<Author> listByIds(@NonNull Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Author> authors = repository.findByIdIn(ids);
        final Map<UUID, Author> result = new HashMap<>(ids.size());
        authors.forEach(author -> result.put(author.id(), author));
        if (ids.size() == result.size()) {
            return result.values();
        }
        throw SetUtils.difference(ids, result.keySet()).stream()
            .map(AuthorService::notFoundAuthorById)
            .collect(BaseAppException.toListCollector());
    }

    private static AppException notFoundAuthorById(UUID id) {
        return BaseAppException.notFoundById(EntityType.AUTHOR, id);
    }
}
