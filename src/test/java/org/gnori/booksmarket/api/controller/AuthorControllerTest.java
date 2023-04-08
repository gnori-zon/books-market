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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.AuthorDtoFactory;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.gnori.booksmarket.storage.entity.BookEntity;
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

@TestInstance(Lifecycle.PER_CLASS)
@SpringJUnitWebConfig
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthorController.class)
public class AuthorControllerTest {

  @MockBean
  AuthorDao authorDao;

  @MockBean
  AuthorDtoFactory authorDtoFactory;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private final static String AUTHOR_URL = "/api/authors";

  private String firstName;
  private String lastName;
  private AuthorEntity raw;
  private Long id;

  @BeforeAll
  void init() {
    firstName = "Petr";
    lastName = "Ivanov";
    raw = AuthorEntity.builder().id(1L).firstName("Petr").lastName("Ivanov").build();
    id = 1L;
  }

  @Test
  public void fetchAuthorsShouldReturnPageOfAuthorsDtoAndOk() throws Exception {

    var raw = new PageImpl<>(
        List.of(
            AuthorEntity.builder().id(1L).firstName(firstName).lastName(lastName).build(),
            AuthorEntity.builder().id(2L).firstName("Petr").lastName("Ivanov").build()));

    var expected = new PageImpl<>(
        List.of(
            AuthorDto.builder().id(1L).firstName(firstName).lastName(lastName).build(),
            AuthorDto.builder().id(2L).firstName("Petr").lastName("Ivanov").build()));

    when(authorDao.findAll((Pageable) Mockito.any())).thenReturn(raw);
    when(authorDtoFactory.createPageOfAuthorDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(get(AUTHOR_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2)))
        .andExpect(jsonPath("$..first_name", contains(firstName, "Petr")))
        .andExpect(jsonPath("$..last_name", contains(lastName, "Ivanov")));
  }

  @Test
  public void createAuthorShouldReturnAuthorDtoAndOk() throws Exception {

    var expected = AuthorDto.builder().id(1L).firstName(firstName).lastName(lastName).build();

    when(authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName))
        .thenReturn(false);
    when(authorDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(authorDtoFactory.createAuthorDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(AUTHOR_URL + "?first_name=" + firstName + "&last_name=" + lastName))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.first_name", is(firstName)))
        .andExpect(jsonPath("$.last_name", is(lastName)));
  }

  @Test
  public void createAuthorShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "The \"first_name\" or \"last_name\" parameter is empty.";

    mockMvc.perform(put(AUTHOR_URL + "?first_name=" + firstName))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verifyNoInteractions(authorDao);
  }

  @Test
  public void createAuthorShouldThrowBadRequestExceptionAndBadRequest2() throws Exception {

    final String exceptionMessage = "Author \"%s %s\" already exists.";

    when(
        authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)).thenReturn(
        true);

    mockMvc.perform(put(
            AUTHOR_URL + "?first_name=" + firstName + "&last_name=" + lastName))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, firstName, lastName),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  public void updateAuthorShouldReturnAuthorDtoAndOk() throws Exception {

    var expected = AuthorDto.builder().id(id).firstName("Petr").lastName("Ivanov").build();

    when(authorDao.findById(id)).thenReturn(Optional.of(raw));
    when(
        authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)).thenReturn(
        false);
    when(authorDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(authorDtoFactory.createAuthorDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(
            AUTHOR_URL + "?id=" + id + "&first_name=" + firstName + "&last_name=" + lastName))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.first_name", is(firstName)))
        .andExpect(jsonPath("$.last_name", is(lastName)));
  }

  @Test
  public void updateAuthorShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "The \"first_name\" and \"last_name\" parameter is empty.";

    mockMvc.perform(put(AUTHOR_URL + "?id=" + id))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verifyNoInteractions(authorDao);
  }

  @Test
  public void updateAuthorShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Author with id: %d does not exists.";

    when(authorDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(
            AUTHOR_URL + "?id=" + id + "&first_name=" + firstName + "&last_name=" + lastName))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  public void updateAuthorShouldThrowBadRequestExceptionAndBadRequest2() throws Exception {

    final String exceptionMessage = "Author \"%s %s\" already exists.";

    when(authorDao.findById(id)).thenReturn(Optional.of(raw));
    when(
        authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)).thenReturn(
        true);

    mockMvc.perform(
            put("/api/authors?id=" + id + "&first_name=" + firstName + "&last_name=" + lastName))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, firstName, lastName),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  public void deleteAuthorShouldReturnOk() throws Exception {

    raw.setBooks(Collections.emptyList());

    when(authorDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(AUTHOR_URL + "/" + id))
        .andExpect(status().isOk());

    verify(authorDao).deleteById(id);
  }

  @Test
  public void deleteShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Author with id: %d doesn't exist.";

    when(authorDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(delete(AUTHOR_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(authorDao, never()).deleteById(id);
  }

  @Test
  public void deleteShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "Author with id: %d cannot be deleted. There are books by this author.";

    raw.setBooks(List.of(new BookEntity()));

    when(authorDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(AUTHOR_URL + "/" + id))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(authorDao, never()).deleteById(id);
  }

}
