package org.gnori.booksmarket.feauture.genre;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenreDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name) {
    public static GenreDto from(Genre genre) {
        return GenreDto.builder()
                .id(genre.id())
                .name(genre.name())
                .build();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreateDto(
            @JsonProperty("name") String name) {
    }
}
