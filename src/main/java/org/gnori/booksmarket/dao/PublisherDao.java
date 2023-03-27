package org.gnori.booksmarket.dao;

import java.util.Optional;
import org.gnori.booksmarket.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherDao extends JpaRepository<PublisherEntity, Long> {
  Optional<PublisherEntity> findByName(String name);
}
