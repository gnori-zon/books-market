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

    return new PageImpl<AuthorDto>(pageOfEntities.map(this::createAuthorDtoFrom).toList());
  }

  public AuthorDto createAuthorDtoFrom(AuthorEntity author) {

    return AuthorDto.builder()
        .id(author.getId())
        .firstName(author.getFirstName())
        .lastName(author.getLastName())
        .books(author.getBooks().stream().map(
            books -> BookDto.builder()
                .id(books.getId())
                .name(books.getName())
                .build()
        ).collect(Collectors.toList()))
        .build();
  }
}
