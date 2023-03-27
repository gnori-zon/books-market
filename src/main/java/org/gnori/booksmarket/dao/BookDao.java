package org.gnori.booksmarket.dao;

import org.gnori.booksmarket.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDao extends JpaRepository<BookEntity,Long> {

}
