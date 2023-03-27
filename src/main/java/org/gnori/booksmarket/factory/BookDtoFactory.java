package org.gnori.booksmarket.factory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.dto.BookDto;
import org.gnori.booksmarket.dto.ReviewDto;
import org.gnori.booksmarket.entity.BookEntity;
import org.gnori.booksmarket.entity.enums.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookDtoFactory {

  public Page<BookDto> createPageOfBookDtoFrom(Page<BookEntity> pageOfEntities) {

    return new PageImpl<BookDto>(pageOfEntities.stream().map(entity -> BookDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .releaseDate(entity.getReleaseDate())
        .publisher(entity.getPublisher().getName())
        .language(entity.getLanguage().getLanguage())
        .reviews(entity.getReviews().stream().map(
            review-> ReviewDto.builder()
                .title(review.getTitle())
                .content(review.getContent())
                .build()
          ).collect(Collectors.toList()))
        .authors(entity.getAuthors().stream().map(
            author -> author.getFirstName() + " " + author.getLastName()
          ).collect(Collectors.toList()))
        .genres(entity.getGenres().stream().map(
            genre -> genre.getName()
          ).collect(Collectors.toList()))
        .build()).collect(Collectors.toList()));
  }

}
