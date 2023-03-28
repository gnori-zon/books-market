package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {

    var pageOfEntities = bookDao.findAll(PageRequest.of(page, size));

    return bookDtoFactory.createPageOfBookDtoFrom(pageOfEntities);
  }
  @PostMapping
  @ResponseStatus(value = HttpStatus.CREATED)
  void createBook(
      @RequestParam String name,
      @RequestParam String description,
      @RequestParam(name = "author_ids") List<Long> authorIds,
      @RequestParam(name = "genre_ids") List<Long> genreIds,
      @RequestParam(name = "release_date") String releaseDate,
      @RequestParam String language,
      @RequestParam(name = "publisher_id") Long publisherId) throws ParseException {

    var sdf = new SimpleDateFormat(DATE_PATTERN);

    if (name.trim().isEmpty()) {
      throw new BadRequestException("The \"name\" parameter is empty.");
    }

    var newGenres = genreDao.findAllById(genreIds);
    if (newGenres.isEmpty()) {
      throw new NotFoundException(String.format("Genres with id: %s not founded", genreIds));
    }

    var newAuthors = authorDao.findAllById(authorIds);
    if (newAuthors.isEmpty()) {
      throw new NotFoundException(String.format("Authors with id: %s not founded", authorIds));
    }

    var newPublisher = publisherDao.findById(publisherId);
    if (newPublisher.isEmpty()) {
      throw new NotFoundException(String.format("Publisher with id: %d not founded", publisherId));
    }

    var newLanguage = Arrays.stream(Language.values()).filter(
            lang -> language.equalsIgnoreCase(lang.getLanguage()))
        .findFirst().orElse(null);
    if (newLanguage == null) {
      throw new NotFoundException(String.format("Language: %s not founded", language));
    }

    name = processName(name);

    bookDao.saveAndFlush(BookEntity.builder()
        .name(name)
        .description(description)
        .language(newLanguage)
        .releaseDate(sdf.parse(releaseDate))
        .genres(newGenres)
        .authors(newAuthors)
        .publisher(newPublisher.get())
        .build());
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
