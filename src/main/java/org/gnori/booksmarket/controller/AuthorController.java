package org.gnori.booksmarket.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.dao.AuthorDao;
import org.gnori.booksmarket.dto.AuthorDto;
import org.gnori.booksmarket.entity.AuthorEntity;
import org.gnori.booksmarket.factory.AuthorDtoFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size){

    var pageOfEntities = authorDao.findAll(PageRequest.of(page,size));

    return authorDtoFactory.createPageOfAuthorDtoFrom(pageOfEntities);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public void createAuthor(
      @RequestParam(name = "first_name") String firstName,
      @RequestParam(name = "last_name") String lastName){

    if (authorDao.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)) {
      //TODO: throw exception
    }
    var newAuthor = AuthorEntity.builder()
        .firstName(firstName)
        .lastName(lastName)
        .build();

    authorDao.save(newAuthor);

  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteAuthor(
      @PathVariable Long id){

    authorDao.deleteById(id);
  }
}
