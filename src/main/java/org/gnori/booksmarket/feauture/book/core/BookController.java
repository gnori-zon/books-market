package org.gnori.booksmarket.feauture.book.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.gnori.booksmarket.core.UUIDV7;
import org.gnori.booksmarket.core.exception.BaseHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService service;

    @GetMapping("/{id}")
    ResponseEntity<BookDto> getById(@PathVariable("id") UUID id) {
        final Book book = service.getById(id);
        return ResponseEntity.ok(BookDto.from(book));
    }

    @PostMapping
    ResponseEntity<BookDto> create(@RequestBody BookDto.CreateDto createDto) {
        final var create = Book.Create.builder()
                .id(UUIDV7.generate())
                .name(StringUtils.defaultString(createDto.name()))
                .description(StringUtils.defaultString(createDto.description()))
                .language(BaseHttpException.requireNotNull("language", Book.Language.fromString(createDto.language())))
                .releaseDate(BaseHttpException.requireNotNull("releaseDate", createDto.releaseDate()))
                .publisherId(BaseHttpException.requireNotNull("publisherId", createDto.publisherId()))
                .authorIds(SetUtils.emptyIfNull(createDto.authorIds()))
                .genreIds(SetUtils.emptyIfNull(createDto.genreIds()))
                .coverAttachmentId(createDto.coverAttachmentId())
                .contentAttachmentId(StringUtils.defaultString(createDto.contentAttachmentId()))
                .build();
        final Book book = service.create(create);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookDto.from(book));
    }

    @PatchMapping("/{id}")
    ResponseEntity<BookDto> updateById(@PathVariable("id") UUID id, @RequestBody ArrayNode patch) {
        final Book book = service.updateById(id, buildBookUpdate(patch));
        return ResponseEntity.ok(BookDto.from(book));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static Book.Update buildBookUpdate(ArrayNode patch) {
        final List<Book.Update.Item> items = new ArrayList<>();
        final Set<UUID> addedGenres = new HashSet<>();
        final Set<UUID> removedGenres = new HashSet<>();
        final Set<UUID> addedAuthors = new HashSet<>();
        final Set<UUID> removedAuthors = new HashSet<>();
        for (JsonNode opValue : patch) {
            if (!opValue.isObject()) {
                continue;
            }
            final ObjectNode op = (ObjectNode) opValue;
            final String operation = op.get("op").asText();
            final String path = op.get("path").asText();
            switch (operation) {
                case "replace": {
                    final JsonNode valueNode = op.get("value");
                    if (valueNode == null || valueNode.isNull()) {
                        throw BaseHttpException.invalidField("value", null);
                    }
                    final String value = valueNode.asText();
                    switch (path) {
                        case "/name" -> items.add(new Book.Update.Item.Name(value));
                        case "/description" -> items.add(new Book.Update.Item.Description(value));
                        case "/releaseDate" -> items.add(new Book.Update.Item.ReleaseDate(LocalDate.parse(value)));
                        case "/language" -> items.add(new Book.Update.Item.Language(Book.Language.fromString(value)));
                        case "/publisher" -> items.add(new Book.Update.Item.Publisher(UUID.fromString(value)));
                        case "/coverAttachmentId" -> items.add(new Book.Update.Item.CoverAttachmentId(value));
                        case "/contentAttachmentId" -> items.add(new Book.Update.Item.ContentAttachmentId(value));
                        default -> throw BaseHttpException.invalidField("path", path);
                    }
                    break;
                }
                case "add": {
                    final Supplier<UUID> parseId = () -> UUID.fromString(path.replaceFirst("/\\w+/([\\w-]+)/?$", "$1"));
                    if (path.matches("/genres/[\\w-]+/?")) {
                        addedGenres.add(parseId.get());
                    } else if (path.matches("/authors/[\\w-]+/?")) {
                        addedAuthors.add(parseId.get());
                    } else {
                        throw BaseHttpException.invalidField("path", path);
                    }
                    break;
                }
                case "remove": {
                    final Supplier<UUID> parseId = () -> UUID.fromString(path.replaceFirst("/\\w+/([\\w-]+)/?$", "$1"));
                    if (path.matches("/genres/[\\w-]+/?")) {
                        removedGenres.add(parseId.get());
                    } else if (path.matches("/authors/[\\w-]+/?")) {
                        removedAuthors.add(parseId.get());
                    } else {
                        throw BaseHttpException.invalidField("path", path);
                    }
                    break;
                }
                default:
                    throw BaseHttpException.invalidField("operation", operation);
            }
        }

        if (!addedAuthors.isEmpty() || !removedAuthors.isEmpty()) {
            items.add(new Book.Update.Item.Authors(addedAuthors, removedAuthors));
        }
        if (!addedGenres.isEmpty() || !removedGenres.isEmpty()) {
            items.add(new Book.Update.Item.Genres(addedGenres, removedGenres));
        }
        return new Book.Update(items);
    }
}
