package org.gnori.booksmarket.api.controller;

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
import org.gnori.booksmarket.api.controller.utils.PageRequestBuilder;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.BookDtoFactory;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.dao.PublisherDao;
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
@RequestMapping("/api/books")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {

  public static final String DEFAULT_PAGE_NUMBER = "0";
  public static final String DEFAULT_PAGE_SIZE = "10";
  public static final String DATE_PATTERN = "YYYY-mm-DD";


  BookDao bookDao;
  BookDtoFactory bookDtoFactory;
  private final GenreDao genreDao;
  private final AuthorDao authorDao;
  private final PublisherDao publisherDao;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  Page<BookDto> fetchBooks(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(required = false, name="author_ids") Optional<List<Long>> authorsId,
      @RequestParam(required = false, name="genre_ids") Optional<List<Long>> genresId,
      @RequestParam(required = false, name="publisher_ids") Optional<List<Long>> publishersId,
      @RequestParam(required = false, name = "sort_by_name") Optional<String> sortByName,
      @RequestParam(required = false,
          name = "sort_by_release_date") Optional<String> sortByReleaseDate) {

    var pageParams = PageRequestBuilder.buildPageRequestForNameAndReleaseDate(page, size,
        sortByName, sortByReleaseDate);

    Page<BookEntity> pageOfEntities;

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

    } else {
      pageOfEntities = bookDao.findAll(pageParams);

    }
    return bookDtoFactory.createPageOfBookDtoFrom(pageOfEntities);
  }

  @PutMapping()
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
        throw new NotFoundException(String.format("Book with id:%d not founded", id.get()));
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
        throw new NotFoundException(String.format("Genres with id: %s not founded", genreIds));
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
      var publisherAuthor = bookEntity.getPublisher().getAuthors();
      authors.forEach(author -> {
        if(!publisherAuthor.contains(author)) publisherAuthor.add(author);
      });
      bookEntity.getPublisher().setAuthors(publisherAuthor);
    }

    if (language.isPresent()) {
      var newLanguage = Arrays.stream(Language.values()).filter(
              lang -> language.get().equalsIgnoreCase(lang.getLanguage()))
          .findFirst().orElse(null);
      if (newLanguage == null) {
        throw new NotFoundException(String.format("Language: %s not founded", language));
      }

      bookEntity.setLanguage(newLanguage);
    }

    if (releaseDate.isPresent()) {
      bookEntity.setReleaseDate(sdf.parse(releaseDate.get()));
    }

    return bookDtoFactory.createBookDtoFrom(bookDao.saveAndFlush(bookEntity));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  void deleteBook(
      @PathVariable Long id) {

    if (!bookDao.existsById(id)) {
      throw new NotFoundException(String.format("Book with id:%d not founded", id));
    }

    bookDao.deleteById(id);
  }
}
