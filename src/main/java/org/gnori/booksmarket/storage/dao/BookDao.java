package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDao extends JpaRepository<BookEntity,Long> {

}
