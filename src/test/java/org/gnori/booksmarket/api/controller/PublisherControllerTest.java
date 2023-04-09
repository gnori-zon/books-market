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
import org.gnori.booksmarket.api.dto.PublisherDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.PublisherDtoFactory;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
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
@WebMvcTest(controllers = PublisherController.class)
class PublisherControllerTest {

  @MockBean
  PublisherDao publisherDao;

  @MockBean
  PublisherDtoFactory publisherDtoFactory;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private static final String PUBLISHER_URL = "/api/publishers";

  private String namePublisher;
  private Long id;
  private PublisherEntity raw;

  @BeforeAll
  void init() {
    id = 1L;
    namePublisher = "ACT";
    raw = PublisherEntity.builder().id(id).name(namePublisher).build();
  }

  @Test
  void fetchPublishersShouldReturnPageOfPublishersDtoAndOk() throws Exception {

    long secondId = 2L;
    var secondNamePublisher = "IBC";

    var raw = new PageImpl<>(List.of(
        this.raw,
        PublisherEntity.builder().id(secondId).name(secondNamePublisher).build()));

    var expected = new PageImpl<>(
        List.of(PublisherDto.builder().id(id).name(namePublisher).build(),
        PublisherDto.builder().id(secondId).name(secondNamePublisher).build()));

    when(publisherDao.findAll((Pageable) Mockito.any())).thenReturn(raw);
    when(publisherDtoFactory.createPageOfPublisherDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(MockMvcRequestBuilders.get(PUBLISHER_URL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2)))
        .andExpect(jsonPath("$..name", contains(namePublisher, secondNamePublisher)));
  }

  @Test
  void createPublisherShouldReturnPublisherDtoAndOk() throws Exception {

    var expected = PublisherDto.builder().id(1L).name(namePublisher).build();

    when(publisherDao.existsByNameIgnoreCase(namePublisher)).thenReturn(false);
    when(publisherDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(publisherDtoFactory.createPublisherDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(PUBLISHER_URL + "?name=" + namePublisher))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(namePublisher)));
  }

  @Test
  void createPublisherShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "The \"name\" parameter is empty.";

    mockMvc.perform(put(PUBLISHER_URL + "?name="))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verifyNoInteractions(publisherDao);
  }

  @Test
  void createPublisherShouldThrowBadRequestExceptionAndBadRequest2() throws Exception {

    final String exceptionMessage = "Publisher \"%s\" already exists.";

    when(publisherDao.existsByNameIgnoreCase(namePublisher)).thenReturn(true);

    mockMvc.perform(put(PUBLISHER_URL + "?name=" + namePublisher))
        .andExpect(status().isBadRequest()).andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, namePublisher),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updatePublisherShouldReturnPublisherDtoAndOk() throws Exception {

    var rawBefore = PublisherEntity.builder().id(id).name("oldName").build();

    var expected = PublisherDto.builder().id(id).name(namePublisher).build();

    when(publisherDao.existsByNameIgnoreCase(namePublisher)).thenReturn(false);
    when(publisherDao.findById(id)).thenReturn(Optional.of(rawBefore));
    when(publisherDao.saveAndFlush(raw)).thenReturn(raw);
    when(publisherDtoFactory.createPublisherDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(PUBLISHER_URL + "?id=" + id + "&name=" + namePublisher))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is(namePublisher)));
  }

  @Test
  void updatePublisherShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Publisher with id: %d doesn't exist";

    when(publisherDao.existsByNameIgnoreCase(namePublisher)).thenReturn(false);
    when(publisherDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(put(PUBLISHER_URL + "?id=" + id + "&name=" + namePublisher))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void deletePublisherShouldReturnOk() throws Exception {

    raw.setAuthors(Collections.emptyList());

    when(publisherDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(PUBLISHER_URL + "/" + id))
        .andExpect(status().isOk());

    verify(publisherDao).deleteById(id);
  }

  @Test
  void deletePublisherShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Publisher with id: %d doesn't exist";

    when(publisherDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(delete(PUBLISHER_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(publisherDao, never()).deleteById(id);
  }

  @Test
  void deletePublisherShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage =
        "Publisher with id: %d cannot be deleted. There are authors by this publisher";

    raw.setAuthors(List.of(new AuthorEntity()));

    when(publisherDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(delete(PUBLISHER_URL + "/" + id))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(publisherDao, never()).deleteById(id);
  }
}