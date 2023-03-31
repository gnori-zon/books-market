package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorDao extends JpaRepository<AuthorEntity, Long> {

  boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

}
