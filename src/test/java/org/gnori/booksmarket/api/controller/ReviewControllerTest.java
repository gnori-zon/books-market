package org.gnori.booksmarket.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.gnori.booksmarket.api.dto.ReviewDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.ReviewDtoFactory;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.ReviewDao;
import org.gnori.booksmarket.storage.entity.ReviewEntity;
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
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;

@TestInstance(Lifecycle.PER_CLASS)
@SpringJUnitWebConfig
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ReviewController.class)
class ReviewControllerTest {

  @MockBean
  ReviewDao reviewDao;

  @MockBean
  ReviewDtoFactory reviewDtoFactory;

  @MockBean
  BookDao bookDao;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private Long bookId;
  private Long id;
  private String title;
  private String content;
  private ReviewEntity raw;

  @BeforeAll
  void init() {
    bookId = 1L;
    id = 1L;
    title = "titleReview";
    content = "contentReview";
    raw = ReviewEntity.builder().id(id).title(title).content(content).build();
  }


  private static final String REVIEW_URL = "/api/reviews";

  @Test
  void fetchReviewsShouldReturnPageOfReviewsDtoAndOk() throws Exception {
    var secondTitle = "secondTitle";
    var secondContent = "secondContent";
    long secondId = 2L;

    var raw = new PageImpl<>(
        List.of(
            this.raw,
            ReviewEntity.builder().id(secondId).title(secondTitle).content(secondContent).build()));

    var expected = new PageImpl<>(
        List.of(
            ReviewDto.builder().id(id).title(title).content(content).build(),
            ReviewDto.builder().id(secondId).title(secondTitle).content(secondContent).build()));

    when(bookDao.existsById(bookId)).thenReturn(true);
    when(reviewDao.findAllByBookId(eq(bookId), Mockito.any())).thenReturn(raw);
    when(reviewDtoFactory.createPageOfReviewsDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(get(REVIEW_URL +"?book_id="+bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..size", contains(2)))
        .andExpect(jsonPath("$..id", contains(1, 2)))
        .andExpect(jsonPath("$..title", contains(title, secondTitle)))
        .andExpect(jsonPath("$..content..content", contains(content, secondContent)));

  }

  @Test
  void fetchReviewsShouldThrowNotFoundExceptionDtoNotFound() throws Exception {

    final String exceptionMessage = "Book with id: %d doesn't exist";

    when(bookDao.existsById(bookId)).thenReturn(false);


    mockMvc.perform(get(REVIEW_URL + "?book_id=" + id))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, bookId),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void createReviewShouldReturnPageOfReviewsDtoAndOk() throws Exception {
    var expected = ReviewDto.builder().id(id).title(title).content(content).build();

    raw.setBookId(bookId);

    when(bookDao.existsById(bookId)).thenReturn(true);
    when(reviewDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(reviewDtoFactory.createReviewDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(REVIEW_URL + "?book_id=" + bookId + "&title=" + title +"&content=" + content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.title", is(title)))
        .andExpect(jsonPath("$.content", is(content)));
  }

  @Test
  void createReviewShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "One parameter is empty";

    when(bookDao.existsById(bookId)).thenReturn(true);


    mockMvc.perform(put(REVIEW_URL + "?book_id=" + id))
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void updateReviewShouldReturnPageOfReviewsDtoAndOk() throws Exception {

    var newTitle = "newTitle";
    var newContent = "newContent";
    var expected = ReviewDto.builder().id(id).title(newTitle).content(newContent).build();

    raw.setBookId(bookId);

    when(bookDao.existsById(bookId)).thenReturn(true);
    when(reviewDao.findById(id)).thenReturn(Optional.of(raw));
    when(reviewDao.saveAndFlush(Mockito.any())).thenReturn(raw);
    when(reviewDtoFactory.createReviewDtoFrom(raw)).thenReturn(expected);

    mockMvc.perform(put(REVIEW_URL + "?book_id=" + bookId + "&title=" + newTitle +"&content=" + newContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.title", is(newTitle)))
        .andExpect(jsonPath("$.content", is(newContent)));
  }

  @Test
  void updateReviewShouldThrowNotFoundExceptionAndOkNotFound() throws Exception {

    final String exceptionMessage = "Book with id: %d doesn't exist";

    when(bookDao.existsById(bookId)).thenReturn(false);

    mockMvc.perform(put(REVIEW_URL + "?book_id=" + bookId +"&content=" + content))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, bookId),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void deleteReviewOk() throws Exception {

    when(reviewDao.existsById(id)).thenReturn(true);

    mockMvc.perform(delete(REVIEW_URL + "/" + id))
        .andExpect(status().isOk());

    verify(reviewDao).deleteById(id);

  }

  @Test
  void deleteReviewShouldThrowNotFoundExceptionAndOkNotFound() throws Exception {

    final String exceptionMessage = "Review with id: %d doesn't exist";

    when(reviewDao.existsById(id)).thenReturn(false);

    mockMvc.perform(delete(REVIEW_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(
            result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }
}