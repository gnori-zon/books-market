package org.gnori.booksmarket.api.factory;

import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.api.dto.GenreDto;
import org.gnori.booksmarket.api.dto.ReviewDto;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

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
                .id(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .build()
          ).collect(Collectors.toList()))
        .authors(entity.getAuthors().stream().map(
            author -> AuthorDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .build()
          ).collect(Collectors.toList()))
        .genres(entity.getGenres().stream().map(
            genre -> GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build()
          ).collect(Collectors.toList()))
        .build()).collect(Collectors.toList()));
  }

}
