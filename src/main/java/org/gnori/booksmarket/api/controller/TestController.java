package org.gnori.booksmarket.api.controller;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.dao.ReviewDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.gnori.booksmarket.storage.entity.GenreEntity;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.gnori.booksmarket.storage.entity.ReviewEntity;
import org.gnori.booksmarket.storage.entity.enums.Language;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping("/api/test")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestController {

  AuthorDao authorDao;
  BookDao bookDao;
  GenreDao genreDao;
  PublisherDao publisherDao;
  ReviewDao reviewDao;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createAll(){
    var author = AuthorEntity.builder()
        .firstName("Petr")
        .lastName("Jeyrin")
        .build();
    authorDao.save(author);

    var genre = GenreEntity.builder()
        .name("History")
        .build();
    genreDao.save(genre);

    var review = ReviewEntity.builder()
        .title("Cool")
        .content("This book very cool!")
        .build();
    reviewDao.save(review);

    var publisher = PublisherEntity.builder()
        .authors(List.of(author))
        .name("IBM")
        .build();
    publisherDao.save(publisher);

    var book = BookEntity.builder()
        .authors(List.of(author))
        .genres(List.of(genre))
        .reviews(List.of(review))
        .publisher(publisher)
        .name("Great book")
        .description("is really greatest book in the world")
        .releaseDate(Date.from(Instant.now()))
        .language(Language.ENG)
        .build();

    bookDao.save(book);

  }
}
