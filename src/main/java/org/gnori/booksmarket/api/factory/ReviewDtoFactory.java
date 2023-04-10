package org.gnori.booksmarket.api.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.dto.ReviewDto;
import org.gnori.booksmarket.storage.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewDtoFactory {

  @LogExecutionTime
  public Page<ReviewDto> createPageOfReviewsDtoFrom(Page<ReviewEntity> pageOfEntities) {

    return new PageImpl<>(pageOfEntities.map(this::createReviewDtoFrom).toList());
  }

  @LogExecutionTime
  public ReviewDto createReviewDtoFrom(ReviewEntity review) {
    return ReviewDto.builder()
        .id(review.getId())
        .title(review.getTitle())
        .content(review.getContent())
        .build();
  }
}
