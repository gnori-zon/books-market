package org.gnori.booksmarket.api.factory;

import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.dto.BookDto;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorDtoFactory {

  public Page<AuthorDto> createPageOfAuthorDtoFrom(Page<AuthorEntity> pageOfEntities) {

    return new PageImpl<AuthorDto>(pageOfEntities.map(entity -> AuthorDto.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .books(entity.getBooks().stream().map(
            book -> BookDto.builder()
                .id(book.getId())
                .name(book.getName())
                .build()
            ).collect(Collectors.toList()))
        .build()).toList());
  }
}
