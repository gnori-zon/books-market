package org.gnori.booksmarket.api.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
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

  @LogExecutionTime
  public Page<BookDto> createPageOfBookDtoFrom(Page<BookEntity> pageOfEntities, String prefixName) {

    return new PageImpl<>(pageOfEntities.stream()
        .filter(book -> book.getName().startsWith(prefixName))
        .map(this::createBookDtoFrom)
        .toList());
  }

  @LogExecutionTime
  public Page<BookDto> createPageOfBookDtoFrom(Page<BookEntity> pageOfEntities) {

    return new PageImpl<>(pageOfEntities.stream().map(this::createBookDtoFrom)
        .toList());
  }

  public BookDto createBookDtoFrom(BookEntity entity) {
    return BookDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .releaseDate(entity.getReleaseDate())
        .publisher(entity.getPublisher().getName())
        .language(entity.getLanguage().getText())
        .reviews(entity.getReviews().stream().map(
            review-> ReviewDto.builder()
                .id(review.getId())
                .title(review.getTitle())
                .content(review.getContent())
                .build()
        ).toList())
        .authors(entity.getAuthors().stream().map(
            author -> AuthorDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .build()
        ).toList())
        .genres(entity.getGenres().stream().map(
            genre -> GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build()
        ).toList())
        .build();
  }
}
