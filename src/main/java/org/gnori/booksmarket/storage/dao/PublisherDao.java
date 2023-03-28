package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherDao extends JpaRepository<PublisherEntity, Long> {
  boolean existsByNameIgnoreCase(String name);
}
