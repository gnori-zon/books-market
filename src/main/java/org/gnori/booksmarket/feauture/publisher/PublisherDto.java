package org.gnori.booksmarket.feauture.publisher;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublisherDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name) {
    public static PublisherDto from(Publisher publisher) {
        return PublisherDto.builder()
                .id(publisher.id())
                .name(publisher.name())
                .build();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreateDto(
            @JsonProperty("name") String name) {
    }
}
