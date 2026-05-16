package org.gnori.booksmarket.feauture.author;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService service;

    @GetMapping("/{id}")
    ResponseEntity<AuthorDto> getById(@PathVariable("id") UUID id) {
        final Author author = service.getById(id);
        return ResponseEntity.ok(AuthorDto.from(author));
    }

    @PostMapping
    ResponseEntity<AuthorDto> create(@RequestBody AuthorDto.CreateDto createDto) {
        final var create = Author.Create.builder()
                .id(UUIDV7.generate())
                .firstName(StringUtils.defaultString(createDto.firstName()))
                .lastName(StringUtils.defaultString(createDto.lastName()))
                .build();
        final Author author = service.create(create);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthorDto.from(author));
    }

    @PatchMapping("/{id}")
    ResponseEntity<AuthorDto> updateById(@PathVariable("id") UUID id, @RequestBody ArrayNode patch) {
        final Author author = service.updateById(id, buildAuthorUpdate(patch));
        return ResponseEntity.ok(AuthorDto.from(author));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static Author.Update buildAuthorUpdate(ArrayNode patch) {
        List<Author.Update.Item> items = new ArrayList<>();

        for (JsonNode opValue : patch) {
            if (!opValue.isObject()) {
                continue;
            }
            final ObjectNode op = (ObjectNode) opValue;
            final String operation = op.get("op").asText();
            final String path = op.get("path").asText();
            switch (operation) {
                case "replace":
                    final JsonNode value = op.get("value");
                    if (value == null || value.isNull()) {
                        throw BaseHttpException.invalidField("value", null);
                    }
                    final String textValue = value.asText();
                    switch (path) {
                        case "/first_name" -> items.add(new Author.Update.Item.FirstName(textValue));
                        case "/last_name" -> items.add(new Author.Update.Item.LastName(textValue));
                        default -> throw BaseHttpException.invalidField("path", textValue);
                    }
                    break;
                default:
                    throw BaseHttpException.invalidField("operation", operation);
            }
        }

        return new Author.Update(items);
    }
}
