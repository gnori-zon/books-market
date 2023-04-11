package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_NUMBER;
import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_SIZE;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.dto.ReviewDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.ReviewDtoFactory;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.dao.ReviewDao;
import org.gnori.booksmarket.storage.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

  private static final String REVIEW_URL = "/api/reviews";

  ReviewDao reviewDao;

  ReviewDtoFactory reviewDtoFactory;

  BookDao bookDao;

  @LogExecutionTime
  @GetMapping(REVIEW_URL)
  @ResponseStatus(HttpStatus.OK)
  public Page<ReviewDto> fetchReviews(
      @RequestParam(name = "book_id") Long bookId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {

    if (page < 0) page = Integer.parseInt(DEFAULT_PAGE_NUMBER);
    if (size < 0) size = Integer.parseInt(DEFAULT_PAGE_SIZE);

    if (!bookDao.existsById(bookId)) {
      throw new NotFoundException(String.format("Book with id: %d doesn't exist", bookId));
    }

    var pageParams = PageRequest.of(page, size);

    var pageOfEntities = reviewDao.findAllByBookId(bookId, pageParams);

    return reviewDtoFactory.createPageOfReviewsDtoFrom(pageOfEntities);
  }

  @LogExecutionTime
  @PutMapping(REVIEW_URL)
  @ResponseStatus(HttpStatus.OK)
  public ReviewDto updateOrCreateReview(
      @RequestParam(name = "book_id") Long bookId,
      @RequestParam(required = false) Optional<Long> id,
      @RequestParam(required = false) Optional<String> title,
      @RequestParam(required = false) Optional<String> content){

    if (!bookDao.existsById(bookId)) {
      throw new NotFoundException(String.format("Book with id: %d doesn't exist", bookId));
    }

    if (id.isPresent()) {

      var optionalReview = reviewDao.findById(id.get());

      if(optionalReview.isEmpty()) {
        throw new NotFoundException(String.format("Review with id: %d doesn't exist", id.get()));
      }

      var review = optionalReview.get();

      title.ifPresent(review::setTitle);
      content.ifPresent(review::setContent);

      return reviewDtoFactory.createReviewDtoFrom(reviewDao.saveAndFlush(review));
    }

    if (title.isEmpty() || content.isEmpty()) {
      throw new BadRequestException("One parameter is empty");
    }

    var review = ReviewEntity.builder()
        .title(title.get())
        .content(content.get())
        .bookId(bookId)
        .build();

    return reviewDtoFactory.createReviewDtoFrom(reviewDao.saveAndFlush(review));
  }

  @LogExecutionTime
  @DeleteMapping(REVIEW_URL+"/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteReview(
      @PathVariable Long id) {

    if (!reviewDao.existsById(id)) {
      throw new NotFoundException(String.format("Review with id: %d doesn't exist", id));
    }

    reviewDao.deleteById(id);
  }

}