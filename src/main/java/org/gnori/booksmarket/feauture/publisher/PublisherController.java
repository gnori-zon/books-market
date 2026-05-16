package org.gnori.booksmarket.feauture.publisher;

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
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService service;

    @GetMapping("/{id}")
    ResponseEntity<PublisherDto> getById(@PathVariable("id") UUID id) {
        final Publisher publisher = service.getById(id);
        return ResponseEntity.ok(PublisherDto.from(publisher));
    }

    @PostMapping
    ResponseEntity<PublisherDto> create(@RequestBody PublisherDto.CreateDto createDto) {
        final var create = Publisher.Create.builder()
                .id(UUIDV7.generate())
                .name(StringUtils.defaultString(createDto.name()))
                .build();
        final Publisher publisher = service.create(create);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PublisherDto.from(publisher));
    }

    @PatchMapping("/{id}")
    ResponseEntity<PublisherDto> updateById(@PathVariable("id") UUID id, @RequestBody ArrayNode patch) {
        final Publisher publisher = service.updateById(id, buildPublisherUpdate(patch));
        return ResponseEntity.ok(PublisherDto.from(publisher));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static Publisher.Update buildPublisherUpdate(ArrayNode patch) {
        List<Publisher.Update.Item> items = new ArrayList<>();
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
                        case "/name" -> items.add(new Publisher.Update.Item.Name(textValue));
                        default -> throw BaseHttpException.invalidField("path", textValue);
                    }
                    break;
                default:
                    throw BaseHttpException.invalidField("operation", operation);
            }
        }

        return new Publisher.Update(items);
    }
}
