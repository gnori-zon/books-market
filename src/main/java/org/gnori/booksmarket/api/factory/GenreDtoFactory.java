package org.gnori.booksmarket.api.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.api.dto.GenreDto;
import org.gnori.booksmarket.storage.entity.GenreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreDtoFactory {

  @LogExecutionTime
  public Page<GenreDto> createPageOfGenreDtoFrom(Page<GenreEntity> pageOfEntities) {

    return new PageImpl<>( pageOfEntities.stream().map(this::createGenreDtoFrom).toList());
  }

  @LogExecutionTime
  public GenreDto createGenreDtoFrom(GenreEntity genre) {

    return GenreDto.builder()
        .id(genre.getId())
        .name(genre.getName())
        .books(genre.getBooks().stream().map(
            book -> BookDto.builder()
                .id(book.getId())
                .name(book.getName())
                .build()
        ).toList())
        .build();
  }
}
