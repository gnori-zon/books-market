package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.dto.PublisherDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.PublisherDtoFactory;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publishers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublisherController {

  public static final String DEFAULT_PAGE_NUMBER = "0";
  public static final String DEFAULT_PAGE_SIZE = "10";

  PublisherDao publisherDao;

  PublisherDtoFactory publisherDtoFactory;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Page<PublisherDto> fetchPublishers(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {

    var pageOfEntities = publisherDao.findAll(PageRequest.of(page, size));

    return publisherDtoFactory.createPageOfPublisherDtoFrom(pageOfEntities);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createPublisher(
      @RequestParam String name) {

    if (name.isEmpty()) {
      throw new BadRequestException("The \"name\" parameter is empty.");
    }

    if (publisherDao.existsByNameIgnoreCase(name)) {
      throw new BadRequestException(String.format("Publisher \"%s\" already exists.", name));
    }

    name = processName(name);

    publisherDao.saveAndFlush(PublisherEntity.builder()
        .name(name)
        .build());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deletePublisher(
      @PathVariable Long id) {

    var optionalPublisher = publisherDao.findById(id);

    if (optionalPublisher.isEmpty()) {
      throw new NotFoundException(String.format("Publisher with id:%d doesn't exist", id));
    }

    if (!optionalPublisher.get().getAuthors().isEmpty()) {
      throw new BadRequestException(String.format(
          "Publisher with id:%d cannot be deleted. There are authors by this publisher", id));
    }

    publisherDao.deleteById(id);
  }


}
