package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_NUMBER;
import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_SIZE;
import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import jakarta.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.controller.utils.PageRequestBuilder;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.BookDtoFactory;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.gnori.booksmarket.storage.entity.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping()
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {

  public static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String BOOK_URL = "/api/books";

  BookDao bookDao;
  GenreDao genreDao;
  AuthorDao authorDao;
  PublisherDao publisherDao;

  BookDtoFactory bookDtoFactory;

  @LogExecutionTime
  @GetMapping(BOOK_URL)
  @ResponseStatus(HttpStatus.OK)
  Page<BookDto> fetchBooks(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(required = false, name="prefix_name") Optional<String> prefixName,
      @RequestParam(required = false, name="author_ids") Optional<List<Long>> authorsId,
      @RequestParam(required = false, name="genre_ids") Optional<List<Long>> genresId,
      @RequestParam(required = false, name="publisher_ids") Optional<List<Long>> publishersId,
      @RequestParam(required = false, name = "sort_by_name") Optional<String> sortByName,
      @RequestParam(required = false,
          name = "sort_by_release_date") Optional<String> sortByReleaseDate) {

    if (page < 0) page = Integer.parseInt(DEFAULT_PAGE_NUMBER);
    if (size < 0) size = Integer.parseInt(DEFAULT_PAGE_SIZE);

    var pageParams = PageRequestBuilder.buildPageRequestForNameAndReleaseDate(page, size,
        sortByName, sortByReleaseDate);

    Page<BookEntity> pageOfEntities;
    boolean isOnlyPrefix = false;

    if (authorsId.isPresent() && genresId.isPresent() && publishersId.isPresent()){
      pageOfEntities = bookDao.findAllByAuthorIdsAndGenreIdsAndPublisherIds(authorsId.get(),
          genresId.get(), publishersId.get(), pageParams);

    } else if (authorsId.isPresent() && genresId.isPresent()) {
      pageOfEntities = bookDao.findAllByGenreIdsAndAuthorIds(genresId.get(),
          authorsId.get(), pageParams);

    } else if (genresId.isPresent() && publishersId.isPresent()) {
      pageOfEntities = bookDao.findAllByGenreIdsAndPublisherIds(genresId.get(),
          publishersId.get(), pageParams);

    } else if (authorsId.isPresent() && publishersId.isPresent()) {
      pageOfEntities = bookDao.findAllByAuthorIdsAndPublisherIds(authorsId.get(),
          publishersId.get(), pageParams);

    } else if (authorsId.isPresent()) {
      pageOfEntities = bookDao.findAllByAuthorIds(authorsId.get(), pageParams);

    } else if (genresId.isPresent()) {
      pageOfEntities = bookDao.findAllByGenreIds(genresId.get(), pageParams);

    } else if (publishersId.isPresent()) {
      pageOfEntities = bookDao.findAllByPublisherIds(publishersId.get(), pageParams);

    } else if (prefixName.isPresent()){
      // when used only prefix
      isOnlyPrefix = true;

      pageOfEntities = bookDao.findAllByNameStartingWith(processName(prefixName.get()), pageParams);

    } else {
      pageOfEntities = bookDao.findAll(pageParams);
    }

    if (!isOnlyPrefix && prefixName.isPresent()) {
      // when used more than just a prefix
      return bookDtoFactory.createPageOfBookDtoFrom(pageOfEntities, processName(prefixName.get()));
    }

    return bookDtoFactory.createPageOfBookDtoFrom(pageOfEntities);
  }

  @LogExecutionTime
  @PutMapping(BOOK_URL)
  @ResponseStatus(HttpStatus.OK)
  public BookDto updateBook(
      @RequestParam(required = false) Optional<Long> id,
      @RequestParam(required = false) Optional<String> name,
      @RequestParam(required = false) Optional<String> description,
      @RequestParam(required = false, name = "author_ids") Optional<List<Long>> authorIds,
      @RequestParam(required = false, name = "genre_ids") Optional<List<Long>> genreIds,
      @RequestParam(required = false, name = "release_date") Optional<String> releaseDate,
      @RequestParam(required = false) Optional<String> language,
      @RequestParam(required = false, name = "publisher_id") Optional<Long> publisherId)
      throws ParseException {

    if (id.isEmpty() && (name.isEmpty() || description.isEmpty() || authorIds.isEmpty() ||
        genreIds.isEmpty() || releaseDate.isEmpty() ||
        language.isEmpty() || publisherId.isEmpty())) {
      throw new BadRequestException("One parameter is empty");
    }

    var sdf = new SimpleDateFormat(DATE_PATTERN);

    var bookEntity = new BookEntity();
    if(id.isPresent()) {
      var optionalBook = bookDao.findById(id.get());
      if(optionalBook.isEmpty()) {
        throw new NotFoundException(String.format("Book with id: %d not founded", id.get()));
      }

      bookEntity = optionalBook.get();
    }

    if (name.isPresent()) {
      if (name.get().trim().isEmpty()) {
        throw new BadRequestException("The \"name\" parameter is empty.");
      }

      bookEntity.setName(processName(name.get()));
    }

    if (description.isPresent()) {
      bookEntity.setDescription(description.get());
    }

    if (genreIds.isPresent()) {
      var newGenres = genreDao.findAllById(genreIds.get());

      if (newGenres.isEmpty()) {
        throw new NotFoundException(String.format("Genres with id: %s not founded", genreIds.get()));
      }

      bookEntity.setGenres(newGenres);
    }

    if (authorIds.isPresent()) {
      var newAuthors = authorDao.findAllById(authorIds.get());

      if (newAuthors.isEmpty()) {
        throw new NotFoundException(
            String.format("Authors with id: %s not founded", authorIds.get()));
      }

      bookEntity.setAuthors(newAuthors);
    }

    if (publisherId.isPresent()) {
      var newPublisher = publisherDao.findById(publisherId.get());
      if (newPublisher.isEmpty()) {
        throw new NotFoundException(
            String.format("Publisher with id: %d not founded", publisherId.get()));
      }
      bookEntity.setPublisher(newPublisher.get());

      var authors = bookEntity.getAuthors();
      var publisherAuthors = bookEntity.getPublisher().getAuthors();
      authors.forEach(author -> {
        if(!publisherAuthors.contains(author)) publisherAuthors.add(author);
      });
      bookEntity.getPublisher().setAuthors(publisherAuthors);
    }

    if (language.isPresent()) {
      var newLanguage = Arrays.stream(Language.values()).filter(
              lang -> language.get().equalsIgnoreCase(lang.getText()))
          .findFirst().orElse(null);
      if (newLanguage == null) {
        throw new NotFoundException(String.format("Language: %s not founded", language.get()));
      }

      bookEntity.setLanguage(newLanguage);
    }

    if (releaseDate.isPresent()) {
      bookEntity.setReleaseDate(sdf.parse(releaseDate.get()));
    }

    return bookDtoFactory.createBookDtoFrom(bookDao.saveAndFlush(bookEntity));
  }

  @LogExecutionTime
  @DeleteMapping(BOOK_URL+"/{id}")
  @ResponseStatus(HttpStatus.OK)
  void deleteBook(
      @PathVariable Long id) {

    if (!bookDao.existsById(id)) {
      throw new NotFoundException(String.format("Book with id: %d not founded", id));
    }
    var bookEntity = bookDao.findById(id).get();
    var authorIds = bookEntity.getAuthors().stream().map(AuthorEntity::getId);
    var publisher = bookEntity.getPublisher();

    bookDao.deleteById(id);

    authorIds.forEach(authorId -> {
      if (!bookDao.existsByPublisherIdAndAuthorId(publisher.getId(), authorId)) {
        publisher.getAuthors()
            .removeIf(authorEntity -> authorId.equals(authorEntity.getId()));
      }
    });
    publisherDao.saveAndFlush(publisher);
  }
}
