package org.gnori.booksmarket.feauture.publisher;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.gnori.booksmarket.core.EntityType;
import org.gnori.booksmarket.core.exception.AppException;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublisherService {
    private final PublisherRepository repository;

    public Publisher getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFoundPublisherById(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Publisher create(Publisher.Create create) {
        create.validate();
        validateUniquenessName(create.name());
        repository.create(create);
        return create.toPublisher();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Publisher updateById(UUID id, Publisher.Update update) {
        update.validate();
        final var exists = repository.findById(id).orElseThrow(() -> notFoundPublisherById(id));
        if (!exists.hasDifferences(update)) {
            return exists;
        }
        if (update.contains(Publisher.Update.Item.Name.class)) {
            final var updateItem = update.get(Publisher.Update.Item.Name.class);
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
            throw notFoundPublisherById(id);
        }
    }

    private void validateUniquenessName(String newName) {
        if (repository.findByName(newName).isPresent()) {
            throw BaseAppException.uniqueConstraintViolation("name");
        }
    }

    private static AppException notFoundPublisherById(UUID id) {
        return BaseAppException.notFoundById(EntityType.PUBLISHER, id);
    }
}
