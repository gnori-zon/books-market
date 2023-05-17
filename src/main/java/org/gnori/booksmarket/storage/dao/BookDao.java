package org.gnori.booksmarket.storage.dao;

import java.util.List;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDao extends JpaRepository<BookEntity,Long> {

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_author ba on book.id = ba.book_id "
      + "where ba.author_id in :ids")
  Page<BookEntity> findAllByAuthorIds(@Param("ids") List<Long> authorIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_genre bg on book.id = bg.book_id "
      + "where bg.genre_id in :ids")
  Page<BookEntity> findAllByGenreIds(@Param("ids") List<Long> genreIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_genre bg on book.id = bg.book_id "
      + "left join book_author ba on book.id = ba.book_id "
      + "where bg.genre_id in :g_ids and ba.author_id in :a_ids")
  Page<BookEntity> findAllByGenreIdsAndAuthorIds(@Param("g_ids") List<Long> genreIds,
      @Param("a_ids")List<Long> authorIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join publisher p on book.publisher_id = p.id "
      + "where p.id in :ids")
  Page<BookEntity> findAllByPublisherIds(@Param("ids") List<Long> publisherIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_author ba on book.id = ba.book_id "
      + "left join publisher p on book.publisher_id = p.id "
      + "where p.id in :p_ids and ba.author_id in :a_ids")
  Page<BookEntity> findAllByAuthorIdsAndPublisherIds(@Param("a_ids")List<Long> authorIds,
      @Param("p_ids") List<Long> publisherIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_genre bg on book.id = bg.book_id "
      + "left join publisher p on book.publisher_id = p.id "
      + "where p.id in :p_ids and bg.genre_id in :g_ids")
  Page<BookEntity> findAllByGenreIdsAndPublisherIds(@Param("g_ids") List<Long> genreIds,
      @Param("p_ids") List<Long> publisherIds, Pageable pageable);

  @Query(nativeQuery = true, value = "select book.* "
      + "from book left join book_author ba on book.id = ba.book_id "
      + "left join book_genre bg on book.id = bg.book_id "
      + "left join publisher p on book.publisher_id = p.id "
      + "where p.id in :p_ids and bg.genre_id in :g_ids and ba.author_id in :a_ids")
  Page<BookEntity> findAllByAuthorIdsAndGenreIdsAndPublisherIds(@Param("a_ids")List<Long> authorIds,
      @Param("g_ids") List<Long> genreIds, @Param("p_ids") List<Long> publisherIds, Pageable pageable);

  Page<BookEntity> findAllByNameStartingWith(String prefix, Pageable pageable);
  @Query(nativeQuery = true, value = "select exists(select book.* "
      + "from book left join book_author ba on book.id = ba.book_id "
      + "where book.publisher_id = :p_id and  ba.author_id = :a_id)")
  boolean existsByPublisherIdAndAuthorId(@Param("p_id") Long publisherId, @Param("a_id")Long authorId);
}
