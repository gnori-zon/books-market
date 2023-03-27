package org.gnori.booksmarket.factory;

import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.dto.AuthorDto;
import org.gnori.booksmarket.entity.AuthorEntity;
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
            book -> book.getName()
        ).collect(Collectors.toList()))
        .build()).toList());
  }
}
