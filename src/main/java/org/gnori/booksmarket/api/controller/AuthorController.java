package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.controller.utils.PageRequestBuilder;
import org.gnori.booksmarket.api.dto.AuthorDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.AuthorDtoFactory;
import org.gnori.booksmarket.storage.dao.AuthorDao;
import org.gnori.booksmarket.storage.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorController {

  public static final String DEFAULT_PAGE_NUMBER = "0";
  public static final String DEFAULT_PAGE_SIZE = "10";

  AuthorDao authorDao;

  AuthorDtoFactory authorDtoFactory;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Page<AuthorDto> fetchAuthors(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(required = false, name = "sort_by_first_name") Optional<String> sortByFirstName,
      @RequestParam(required = false, name = "sort_by_last_name") Optional<String> sortByLastName) {

    var pageParams = PageRequestBuilder.buildPageRequestForFirstAndLastName(page, size,
        sortByFirstName, sortByLastName);

    var pageOfEntities = authorDao.findAll(pageParams);

    return authorDtoFactory.createPageOfAuthorDtoFrom(pageOfEntities);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuthorDto createAuthor(
      @RequestParam(name = "first_name") String firstName,
      @RequestParam(name = "last_name") String lastName) {

    if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
      throw new BadRequestException("The \"first_name\" or \"last_name\" parameter is empty.");
    }

    if (authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)) {
      throw new BadRequestException(
          String.format("Author \"%s %s\" already exists.", firstName, lastName)
      );
    }

    firstName = processName(firstName);

    lastName = processName(lastName);

    return authorDtoFactory.createAuthorDtoFrom(authorDao.saveAndFlush(AuthorEntity.builder()
        .firstName(firstName)
        .lastName(lastName)
        .build()));

  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public AuthorDto updateAuthor(
      @PathVariable Long id,
      @RequestParam(required = false, name = "first_name") Optional<String> firstName,
      @RequestParam(required = false, name = "last_name") Optional<String> lastName) {

    if (firstName.isEmpty() && lastName.isEmpty()) {
      throw new BadRequestException("The \"first_name\" and \"last_name\" parameter is empty.");
    }

    var optionalAuthor = authorDao.findById(id);

    if (optionalAuthor.isEmpty()) {
      throw new NotFoundException(
          String.format("Author with id:%d does not exists.", id)
      );
    }

    var author = optionalAuthor.get();

    if (firstName.isPresent() && !firstName.get().trim().isEmpty()) {
      var newFirstName = processName(firstName.get());
      author.setFirstName(newFirstName);
    }

    if (lastName.isPresent() && !lastName.get().trim().isEmpty()) {
      var newLastName = processName(lastName.get());
      author.setLastName(newLastName);
    }

    if (authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(
        author.getFirstName(), author.getLastName())) {
      throw new BadRequestException(
          String.format("Author \"%s %s\" already exists.", firstName, lastName)
      );
    }

    return authorDtoFactory.createAuthorDtoFrom(authorDao.saveAndFlush(author));

  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteAuthor(
      @PathVariable Long id) {

    var optionalAuthor = authorDao.findById(id);
    if (optionalAuthor.isEmpty()) {
      throw new NotFoundException(String.format("Author with id: %d doesn't exist.", id));
    }
    if (!optionalAuthor.get().getBooks().isEmpty()) {
      throw new BadRequestException(String.format(
          "Author with id:%d cannot be deleted. There are books by this author.", id));
    }

    authorDao.deleteById(id);
  }

}
