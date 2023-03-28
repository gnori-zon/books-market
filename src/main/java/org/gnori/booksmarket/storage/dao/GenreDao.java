package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreDao extends JpaRepository<GenreEntity, Long> {

  boolean existsByNameIgnoreCase(String name);

}
