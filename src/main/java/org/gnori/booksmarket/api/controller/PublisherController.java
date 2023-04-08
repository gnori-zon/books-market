package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_NUMBER;
import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_SIZE;
import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.controller.utils.PageRequestBuilder;
import org.gnori.booksmarket.api.dto.PublisherDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.PublisherDtoFactory;
import org.gnori.booksmarket.storage.dao.PublisherDao;
import org.gnori.booksmarket.storage.entity.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publishers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublisherController {

  PublisherDao publisherDao;

  PublisherDtoFactory publisherDtoFactory;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Page<PublisherDto> fetchPublishers(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(required = false, name = "sort_by_name") Optional<String> sortByName) {

    if (page < 0) page = Integer.parseInt(DEFAULT_PAGE_NUMBER);
    if (size < 0) size = Integer.parseInt(DEFAULT_PAGE_SIZE);

    var pageParams = PageRequestBuilder.buildPageRequestForName(page, size, sortByName);

    var pageOfEntities = publisherDao.findAll(pageParams);

    return publisherDtoFactory.createPageOfPublisherDtoFrom(pageOfEntities);
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public PublisherDto updateOrCreatePublisher(
      @RequestParam(required = false) Optional<Long> id,
      @RequestParam String name) {

    if (name.isEmpty()) {
      throw new BadRequestException("The \"name\" parameter is empty.");
    }

    if (publisherDao.existsByNameIgnoreCase(name)) {
      throw new BadRequestException(String.format("Publisher \"%s\" already exists.", name));
    }

    name = processName(name);

    var publisherEntity = PublisherEntity.builder()
        .name(name)
        .build();

    if(id.isPresent()) {
      var optionalPublisher = publisherDao.findById(id.get());

      if(optionalPublisher.isEmpty()) {
        throw new NotFoundException(String.format("Publisher with id:%d doesn't exist", id.get()));
      } else {
        publisherEntity = optionalPublisher.get();

        publisherEntity.setName(name);
      }
    }

    return publisherDtoFactory.createPublisherDtoFrom(publisherDao.saveAndFlush(publisherEntity));
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
