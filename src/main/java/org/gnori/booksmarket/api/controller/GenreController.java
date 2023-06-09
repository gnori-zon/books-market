package org.gnori.booksmarket.api.controller;

import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_NUMBER;
import static org.gnori.booksmarket.api.controller.AuthorController.DEFAULT_PAGE_SIZE;
import static org.gnori.booksmarket.api.controller.utils.NameUtils.processName;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.gnori.booksmarket.api.controller.utils.PageRequestBuilder;
import org.gnori.booksmarket.api.dto.GenreDto;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.api.factory.GenreDtoFactory;
import org.gnori.booksmarket.storage.dao.GenreDao;
import org.gnori.booksmarket.storage.entity.GenreEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {

  private static final String GENRE_URL = "/api/genres";

  GenreDao genreDao;

  GenreDtoFactory genreDtoFactory;

  @LogExecutionTime
  @GetMapping(GENRE_URL)
  @ResponseStatus(HttpStatus.OK)
  public Page<GenreDto> fetchGenres(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
      @RequestParam(required = false, name = "sort_by_name") Optional<String> sortByName) {

    if (page < 0) page = Integer.parseInt(DEFAULT_PAGE_NUMBER);
    if (size < 0) size = Integer.parseInt(DEFAULT_PAGE_SIZE);

    var pageParams = PageRequestBuilder.buildPageRequestForName(page, size, sortByName);

    var pageOfEntities = genreDao.findAll(pageParams);

    return genreDtoFactory.createPageOfGenreDtoFrom(pageOfEntities);
  }

  @LogExecutionTime
  @PutMapping(GENRE_URL)
  @ResponseStatus(HttpStatus.OK)
  public GenreDto updateOrCreateGenre(
      @RequestParam(required = false) Optional<Long> id,
      @RequestParam String name){

    if (name.trim().isEmpty() || name.length() < 2) {
      throw new BadRequestException("The \"name\" parameter is less than two or empty.");
    }

    if (genreDao.existsByNameIgnoreCase(name)) {
      throw new BadRequestException(String.format("Genre \"%s\" already exists.", name));
    }

    name = processName(name);

    var genreEntity = GenreEntity.builder()
        .name(name)
        .build();

    if (id.isPresent()) {
      var optionalGenre = genreDao.findById(id.get());

      if (optionalGenre.isEmpty()){
        throw new NotFoundException(String.format("Genre with id: %d doesn't exist", id.get()));
      } else {
        genreEntity = optionalGenre.get();

        genreEntity.setName(name);
      }
    }

    return genreDtoFactory.createGenreDtoFrom(genreDao.saveAndFlush(genreEntity));
  }

  @LogExecutionTime
  @DeleteMapping(GENRE_URL+"/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteGenre(
      @PathVariable Long id) {

    var optionalGenre = genreDao.findById(id);

    if (optionalGenre.isEmpty()) {
      throw new NotFoundException(String.format("Genre with id: %d doesn't exist", id));
    }

    if (!optionalGenre.get().getBooks().isEmpty()) {
      throw new BadRequestException(String.format(
          "Genre with id: %d cannot be deleted. There are books by this genre", id));
    }

    genreDao.deleteById(id);
  }

}
