package org.gnori.booksmarket.api.factory;

import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

  public Page<GenreDto> createPageOfGenreDtoFrom(Page<GenreEntity> pageOfEntities) {
    return new PageImpl<GenreDto>( pageOfEntities.stream().map(genreEntity -> GenreDto.builder()
            .id(genreEntity.getId())
            .name(genreEntity.getName())
            .books(genreEntity.getBooks().stream().map(
                book -> BookDto.builder()
                    .id(book.getId())
                    .name(book.getName())
                    .build()
            ).collect(Collectors.toList()))
            .build()
        ).collect(Collectors.toList()));
  }
}
