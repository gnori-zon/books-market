package org.gnori.booksmarket.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.dao.AuthorDao;
import org.gnori.booksmarket.dao.BookDao;
import org.gnori.booksmarket.dao.GenreDao;
import org.gnori.booksmarket.dao.PublisherDao;
import org.gnori.booksmarket.dto.BookDto;
import org.gnori.booksmarket.entity.BookEntity;
import org.gnori.booksmarket.entity.enums.Language;
import org.gnori.booksmarket.factory.BookDtoFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size){

    var pageOfEntities = bookDao.findAll(PageRequest.of(page,size));

    return bookDtoFactory.createPageOfBookDtoFrom(pageOfEntities);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(value = HttpStatus.CREATED)
  void createBook(
      @RequestParam String name,
      @RequestParam String description,
      @RequestParam List<String> authors,
      @RequestParam List<String> genres,
      @RequestParam(name = "release_date") String releaseDate,
      @RequestParam String language,
      @RequestParam String publisher) throws ParseException {

    var sdf = new SimpleDateFormat(DATE_PATTERN);

    var newGenres = genreDao.findByNameIn(genres);
    //if (newGenres.isEmpty()) throw CustomException; TODO: impl throwing

    List<List<String>> names = List.of(new ArrayList<>(),new ArrayList<>());
    for (String raw : authors) {
      var firstAndLastName = raw.split(" ");
      names.get(0).add(firstAndLastName[0]);
      names.get(1).add(firstAndLastName[1]);
    }

    var newAuthors =
        authorDao.findByFirstNameInAndLastNameIn(names.get(0),names.get(1));
    //if (newAuthors.isEmpty()) throw CustomException; TODO: impl throwing

    var newPublisher = publisherDao.findByName(publisher);
    //if (newPublisher.isEmpty()) throw CustomException; TODO: impl throwing

    var newBook = BookEntity.builder()
        .name(name)
        .description(description)
        .language(Arrays.stream(Language.values()).filter(
                lang -> language.equalsIgnoreCase(lang.getLanguage()))
            .findFirst()
            .orElseThrow())
        .releaseDate(sdf.parse(releaseDate))
        .genres(newGenres)
        .authors(newAuthors)
        .publisher(newPublisher.get())
        .build();

    bookDao.save(newBook);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  void deleteBook(
      @PathVariable Long id){

    bookDao.deleteById(id);
  }
}
