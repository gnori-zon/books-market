package org.gnori.booksmarket.dao;

import java.util.List;
import org.gnori.booksmarket.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreDao extends JpaRepository<GenreEntity, Long> {

  List<GenreEntity> findByNameIn(List<String> name);

}
