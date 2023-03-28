package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.dto.GenreDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.GenreDtoFactory;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.entity.GenreEntity;
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
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {

  public static final String DEFAULT_PAGE_NUMBER = "0";
  public static final String DEFAULT_PAGE_SIZE = "10";

  GenreDao genreDao;

  GenreDtoFactory genreDtoFactory;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Page<GenreDto> fetchGenres(
      @RequestParam(DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(DEFAULT_PAGE_SIZE) Integer size) {

    var pageOfEntities = genreDao.findAll(PageRequest.of(page, size));

    return genreDtoFactory.createPageOfGenreDtoFrom(pageOfEntities);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createGenre(
      @RequestParam String name) {

    if (name.trim().isEmpty() || name.length() < 2) {
      throw new BadRequestException("The \"name\" parameter is less than two or empty.");
    }

    if (genreDao.existsByNameIgnoreCase(name)) {
      throw new BadRequestException(String.format("Genre \"%s\" already exists.", name));
    }

    name = processName(name);

    genreDao.saveAndFlush(GenreEntity.builder()
        .name(name)
        .build());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteGenre(
      @PathVariable Long id) {

    var optionalGenre = genreDao.findById(id);

    if (optionalGenre.isEmpty()) {
      throw new NotFoundException(String.format("Genre with id:%d doesn't exist", id));
    }

    if (!optionalGenre.get().getBooks().isEmpty()) {
      throw new BadRequestException(String.format(
          "Genre with id:%d cannot be deleted. There are books by this genre", id));
    }

    genreDao.deleteById(id);
  }

}
