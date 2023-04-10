package org.gnori.booksmarket.api.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.dto.PublisherDto;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublisherDtoFactory {

  @LogExecutionTime
  public Page<PublisherDto> createPageOfPublisherDtoFrom(Page<PublisherEntity> pageOfEntities) {
    return new PageImpl<>(pageOfEntities.stream().map(this::createPublisherDtoFrom)
        .toList());
  }

  @LogExecutionTime
  public PublisherDto createPublisherDtoFrom(PublisherEntity publisher) {
    return PublisherDto.builder()
        .id(publisher.getId())
        .name(publisher.getName())
        .authors(publisher.getAuthors().stream().map(
            author-> AuthorDto.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .build()
        ).toList())
        .build();
  }

}
