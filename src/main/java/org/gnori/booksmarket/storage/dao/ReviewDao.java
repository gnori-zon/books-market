package org.gnori.booksmarket.storage.dao;

import org.gnori.booksmarket.storage.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewDao extends JpaRepository<ReviewEntity, Long> {

  @Query(nativeQuery = true, value = "select r.* "
      + "from review r left join book b on r.book_id = b.id "
      + "where b.id = :book_id")
  Page<ReviewEntity> findAllByBookId(@Param("book_id") Long bookId, Pageable pageable);

}