package org.gnori.booksmarket.storage.dao;

import java.util.List;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorDao extends JpaRepository<AuthorEntity, Long> {

  List<AuthorEntity> findByFirstNameInAndLastNameIn(List<String> firstName,
      List<String> lastName);

  boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

}
