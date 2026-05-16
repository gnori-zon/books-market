package org.gnori.booksmarket.feauture.publisher;

import java.util.Optional;
import java.util.UUID;

public interface PublisherRepository {
    Optional<Publisher> findById(UUID id);
    void create(Publisher.Create create);
    boolean updateById(UUID id, Publisher.Update update);
    boolean deleteById(UUID id);

    Optional<Object> findByName(String name);
}
