package org.gnori.booksmarket.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.gnori.booksmarket.api.dto.GenreDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.GenreDtoFactory;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.gnori.booksmarket.storage.entity.GenreEntity;
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
@WebMvcTest(controllers = GenreController.class)
class GenreControllerTest {

  @MockBean
  GenreDao genreDao;

  @MockBean
  GenreDtoFactory genreDtoFactory;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private String nameGenre;
  private Long id;
  private GenreEntity raw;

  @BeforeAll
  void init() {
    nameGenre = "History";
    raw = GenreEntity.builder().id(1L).name(nameGenre).build();
    id = 1L;
  }

  private static final String GENRE_URL = "/api/genres";

  @Test
  void fetchGenres() throws Exception {

    var raw = new PageImpl<>(
        List.of(
            GenreEntity.builder().id(1L).name(nameGenre).build(),
            GenreEntity.builder().id(2L).name("Non/fiction").build()));

    var expected = new PageImpl<>(
        List.of(
            GenreDto.builder().id(1L).name("History").build(),
            GenreDto.builder().id(2L).name("Non/fiction").build()));

    when(genreDao.findAll((Pageable) Mockito.any())).thenReturn(raw);
    when(genreDtoFactory.createPageOfGenreDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(MockMvcRequestBuilders.get(GENRE_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2)))
        .andExpect(jsonPath("$..name", contains(nameGenre, "Non/fiction")));
  }

  @Test
  void createGenreShouldReturnGenreDtoAndOk() throws Exception {

    var expected = GenreDto.builder().id(1L).name(nameGenre).build();

    when(genreDao.existsByNameIgnoreCase(nameGenre)).thenReturn(false);
    when(genreDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(genreDtoFactory.createGenreDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(GENRE_URL + "?name=" + nameGenre))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(nameGenre)));
  }

  @Test
  void createGenreShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "The \"name\" parameter is less than two or empty.";

    mockMvc.perform(put(GENRE_URL + "?name="))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verifyNoInteractions(genreDao);
  }

  @Test
  void createGenreShouldThrowBadRequestExceptionAndBadRequest2() throws Exception {

    final String exceptionMessage = "Genre \"%s\" already exists.";

    when(genreDao.existsByNameIgnoreCase(nameGenre)).thenReturn(true);

    mockMvc.perform(put(GENRE_URL + "?name=" + nameGenre))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, nameGenre),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  public void updateGenreShouldReturnGenreDtoAndOk() throws Exception {

    var rawBefore = GenreEntity.builder().id(1L).name("Genre").build();

    var expected = GenreDto.builder().id(1L).name(nameGenre).build();

    when(genreDao.existsByNameIgnoreCase(nameGenre)).thenReturn(false);
    when(genreDao.findById(id)).thenReturn(Optional.of(rawBefore));
    when(genreDao.saveAndFlush(raw)).thenReturn(raw);
    when(genreDtoFactory.createGenreDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(GENRE_URL + "?id=" + id + "&name=" + nameGenre))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(nameGenre)));
  }

  @Test
  public void updateGenreShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Genre with id: %d doesn't exist";

    when(genreDao.existsByNameIgnoreCase(nameGenre)).thenReturn(false);
    when(genreDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(GENRE_URL + "?id=" + id + "&name=" + nameGenre))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  public void deleteGenreShouldReturnOk() throws Exception {

    raw.setBooks(Collections.emptyList());

    when(genreDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(GENRE_URL + "/" + id))
        .andExpect(status().isOk());

    verify(genreDao).deleteById(id);
  }

  @Test
  public void deleteGenreShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Genre with id: %d doesn't exist";

    when(genreDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(delete(GENRE_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(genreDao, never()).deleteById(id);
  }

  @Test
  public void deleteGenreShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "Genre with id: %d cannot be deleted. There are books by this genre";

    raw.setBooks(List.of(new BookEntity()));

    when(genreDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(GENRE_URL + "/" + id))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(genreDao, never()).deleteById(id);
  }
}