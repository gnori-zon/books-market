package org.gnori.booksmarket.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.api.dto.ReviewDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.BookDtoFactory;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.gnori.booksmarket.storage.entity.GenreEntity;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.gnori.booksmarket.storage.entity.ReviewEntity;
import org.gnori.booksmarket.storage.entity.enums.Language;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestInstance(Lifecycle.PER_CLASS)
@SpringJUnitWebConfig
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = BookController.class)
class BookControllerTest {

  @MockBean
  BookDao bookDao;
  @MockBean
  GenreDao genreDao;
  @MockBean
  AuthorDao authorDao;
  @MockBean
  PublisherDao publisherDao;

  @MockBean
  BookDtoFactory bookDtoFactory;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private static final String BOOK_URL = "/api/books";

  private String nameBook;
  private Long id;
  private BookEntity raw;


  @BeforeAll
  void init() {
    id = 1L;
    nameBook = "Philosophy java";
    var description = "It great book";
    var language = Language.ENG;
    var publisher = PublisherEntity.builder().id(3L).name("ACT").build();
    var reviews = List.of(
        ReviewEntity.builder().id(1L).title("First review").content("Bla bla bla bla").build(),
        ReviewEntity.builder().id(2L).title("Lie").content("NO, it's not great book! IT'S THE GREATEST BOOK").build());
    var authors = List.of(
        AuthorEntity.builder().id(2L).firstName("Bruce").lastName("Eckel").build());

    raw = BookEntity.builder().id(id)
        .name(nameBook)
        .description(description)
        .language(language)
        .publisher(publisher)
        .reviews(reviews)
        .authors(authors)
        .build();
  }

  @Test
  void fetchBooksShouldReturnPageOfBooksDtoAndOk() throws Exception {

    long secondId = 2L;
    var secondNameBook = "IBC";

    var raw = new PageImpl<>(List.of(
        this.raw,
        BookEntity.builder().id(secondId).name(secondNameBook).build()));

    var expected = new PageImpl<>(
        List.of(BookDto.builder()
                .id(id).name(nameBook)
                .description(this.raw.getDescription())
                .language(this.raw.getLanguage().getLanguage())
                .publisher(this.raw.getPublisher().getName())
                .reviews(this.raw.getReviews().stream().map(
                    reviews -> ReviewDto.builder()
                        .id(reviews.getId())
                        .title(reviews.getTitle())
                        .content(reviews.getContent())
                        .build()).collect(Collectors.toList()))
            .authors(this.raw.getAuthors().stream().map(
                    authors -> AuthorDto.builder()
                        .id(authors.getId())
                        .firstName(authors.getFirstName())
                        .lastName(authors.getLastName())
                        .build()).collect(Collectors.toList()))
                .build(),
            BookDto.builder().id(secondId).name(secondNameBook).build()));

    when(bookDao.findAll((Pageable) Mockito.any())).thenReturn(raw);
    when(bookDtoFactory.createPageOfBookDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(MockMvcRequestBuilders.get(BOOK_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2, 1, 2, 2)))
        .andExpect(jsonPath("$..name", contains(nameBook, secondNameBook)))
        .andExpect(jsonPath("$..description", contains(this.raw.getDescription())))
        .andExpect(jsonPath("$..authors..first_name", contains(this.raw.getAuthors().get(0).getFirstName())))
        .andExpect(jsonPath("$..authors..last_name", contains(this.raw.getAuthors().get(0).getLastName())))
        .andExpect(jsonPath("$..language", contains(this.raw.getLanguage().getLanguage())))
        .andExpect(jsonPath("$..reviews..title", contains(this.raw.getReviews().get(0).getTitle(),this.raw.getReviews().get(1).getTitle())))
        .andExpect(jsonPath("$..reviews..content", contains(this.raw.getReviews().get(0).getContent(),this.raw.getReviews().get(1).getContent())))
        .andExpect(jsonPath("$..publisher", contains(this.raw.getPublisher().getName())));
  }

  @Test
  void fetchBooksWithClarifyingParametersShouldReturnPageOfBooksDtoAndOk() throws Exception {

    long secondId = 2L;
    var secondNameBook = "IBC";
    var authorsId = List.of(1L,2L);
    var genresId = List.of(1L,2L);
    var publishersId = List.of(1L);
    var prefixName = "Boo";
    var raw = new PageImpl<>(List.of(
        this.raw,
        BookEntity.builder().id(secondId).name(secondNameBook).build()));

    var expected = new PageImpl<>(
        List.of(BookDto.builder().id(id).name(nameBook).build(),
            BookDto.builder().id(secondId).name(secondNameBook).build()));

    when(bookDao.findAllByAuthorIdsAndGenreIdsAndPublisherIds(eq(authorsId), eq(genresId),
        eq(publishersId), Mockito.any())).thenReturn(raw);
    when(bookDtoFactory.createPageOfBookDtoFrom(raw, prefixName)).thenReturn(expected);

    mockMvc.perform(MockMvcRequestBuilders.get(BOOK_URL + "?prefix_name=" + prefixName +
            "&author_ids=1,2&genre_ids=1,2&publisher_ids=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2)))
        .andExpect(jsonPath("$..name", contains(nameBook, secondNameBook)));
  }

  @Test
  void createBookShouldReturnBookDtoAndOk() throws Exception {

    var sdf = new SimpleDateFormat("yyyy-MM-dd");

    var expected = BookDto.builder()
        .id(id).name(nameBook)
        .description(this.raw.getDescription())
        .language(this.raw.getLanguage().getLanguage())
        .publisher(this.raw.getPublisher().getName())
        .reviews(this.raw.getReviews().stream().map(
            reviews -> ReviewDto.builder()
                .id(reviews.getId())
                .title(reviews.getTitle())
                .content(reviews.getContent())
                .build()).collect(Collectors.toList()))
        .authors(this.raw.getAuthors().stream().map(
            authors -> AuthorDto.builder()
                .id(authors.getId())
                .firstName(authors.getFirstName())
                .lastName(authors.getLastName())
                .build()).collect(Collectors.toList()))
        .build();
    raw.setGenres(List.of(GenreEntity.builder().id(1L).build()));

    raw.setReleaseDate(sdf.parse("2020-09-12"));

    when(genreDao.findAllById(
        raw.getGenres().stream().map(GenreEntity::getId).collect(Collectors.toList())))
        .thenReturn(raw.getGenres());
    when(authorDao.findAllById(raw.getAuthors().stream().map(AuthorEntity::getId).collect(Collectors.toList())))
        .thenReturn(raw.getAuthors());
    when(publisherDao.findById(raw.getPublisher().getId()))
        .thenReturn(Optional.of(raw.getPublisher()));

    when(bookDao.saveAndFlush(raw)).thenReturn(raw);
    when(bookDtoFactory.createBookDtoFrom(Mockito.any())).thenReturn(expected);

    mockMvc.perform(put(BOOK_URL + "?name=" + nameBook + "&description=" + raw.getDescription()
                + "&author_ids=2&genre_ids=1&release_date=2020-09-12&language=" + raw.getLanguage().getLanguage() + "&publisher_id=3"
            ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(nameBook)))
        .andExpect(jsonPath("$..description", contains(this.raw.getDescription())))
        .andExpect(jsonPath("$..authors..first_name", contains(this.raw.getAuthors().get(0).getFirstName())))
        .andExpect(jsonPath("$..authors..last_name", contains(this.raw.getAuthors().get(0).getLastName())))
        .andExpect(jsonPath("$..language", contains(this.raw.getLanguage().getLanguage())))
        .andExpect(jsonPath("$..reviews..title", contains(this.raw.getReviews().get(0).getTitle(),this.raw.getReviews().get(1).getTitle())))
        .andExpect(jsonPath("$..reviews..content", contains(this.raw.getReviews().get(0).getContent(),this.raw.getReviews().get(1).getContent())))
        .andExpect(jsonPath("$..publisher", contains(this.raw.getPublisher().getName())));
  }

  @Test
  void createBookShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "One parameter is empty";

    mockMvc.perform(put(BOOK_URL +"?name=&description=&author_ids=&genre_ids=&release_date=&language=&publisher_id="
        ))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verifyNoInteractions(bookDao);
  }

  @Test
  void updateBookShouldReturnBookDtoAndOk() throws Exception {

    var newName = "new Book";
    raw.setName(newName);
    var expected = BookDto.builder()
        .id(id).name(newName)
        .description(this.raw.getDescription())
        .language(this.raw.getLanguage().getLanguage())
        .publisher(this.raw.getPublisher().getName())
        .reviews(this.raw.getReviews().stream().map(
            reviews -> ReviewDto.builder()
                .id(reviews.getId())
                .title(reviews.getTitle())
                .content(reviews.getContent())
                .build()).collect(Collectors.toList()))
        .authors(this.raw.getAuthors().stream().map(
            authors -> AuthorDto.builder()
                .id(authors.getId())
                .firstName(authors.getFirstName())
                .lastName(authors.getLastName())
                .build()).collect(Collectors.toList()))
        .build();

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));
    when(bookDao.saveAndFlush(raw)).thenReturn(raw);
    when(bookDtoFactory.createBookDtoFrom(Mockito.any())).thenReturn(expected);

    mockMvc.perform(put(BOOK_URL + "?id=" + 1 + "&name=" + newName))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(newName)))
        .andExpect(jsonPath("$..description", contains(this.raw.getDescription())))
        .andExpect(jsonPath("$..authors..first_name", contains(this.raw.getAuthors().get(0).getFirstName())))
        .andExpect(jsonPath("$..authors..last_name", contains(this.raw.getAuthors().get(0).getLastName())))
        .andExpect(jsonPath("$..language", contains(this.raw.getLanguage().getLanguage())))
        .andExpect(jsonPath("$..reviews..title", contains(this.raw.getReviews().get(0).getTitle(),this.raw.getReviews().get(1).getTitle())))
        .andExpect(jsonPath("$..reviews..content", contains(this.raw.getReviews().get(0).getContent(),this.raw.getReviews().get(1).getContent())))
        .andExpect(jsonPath("$..publisher", contains(this.raw.getPublisher().getName())));
  }

  @Test
  void updateBookShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Book with id: %d not founded";

    when(bookDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(BOOK_URL +"?id="+id))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateBookShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "The \"name\" parameter is empty.";

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(put(BOOK_URL +"?id="+id+ "&name="))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateBookShouldThrowNotFoundExceptionAndNotFound2() throws Exception {

    final String exceptionMessage = "Genres with id: %s not founded";

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));
    when(genreDao.findAllById(List.of(id))).thenReturn(List.of());

    mockMvc.perform(put(BOOK_URL +"?id="+id +"&genre_ids=1"))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, List.of(id)),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateBookShouldThrowNotFoundExceptionAndNotFound3() throws Exception {

    final String exceptionMessage = "Authors with id: %s not founded";

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));
    when(authorDao.findAllById(List.of(id))).thenReturn(List.of());

    mockMvc.perform(put(BOOK_URL +"?id="+id +"&author_ids=1"))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, List.of(id)),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateBookShouldThrowNotFoundExceptionAndNotFound4() throws Exception {

    final String exceptionMessage = "Publisher with id: %d not founded";

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));
    when(publisherDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(BOOK_URL +"?id="+id +"&publisher_id=1"))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateBookShouldThrowNotFoundExceptionAndNotFound5() throws Exception {

    var language = "MMMM";

    final String exceptionMessage = "Language: %s not founded";

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));
    when(publisherDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(BOOK_URL +"?id="+id +"&language=" + language))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, language),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void deleteBookShouldReturnOk() throws Exception {

    when(bookDao.existsById(id)).thenReturn(true);

    mockMvc.perform(delete(BOOK_URL + "/" + id))
        .andExpect(status().isOk());

    verify(bookDao).deleteById(id);
  }

  @Test
  void deleteBookShouldThrowNotFoundExceptionOkNotFound() throws Exception {

    final String exceptionMessage = "Book with id: %d not founded";

    when(bookDao.existsById(id)).thenReturn(false);

    mockMvc.perform(delete(BOOK_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
    verify(bookDao, never()).deleteById(id);
  }
}