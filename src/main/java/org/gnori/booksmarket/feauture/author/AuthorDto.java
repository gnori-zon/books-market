package org.gnori.booksmarket.feauture.author;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName) {
    public static AuthorDto from(Author author) {
        return AuthorDto.builder()
                .id(author.id())
                .firstName(author.firstName())
                .lastName(author.lastName())
                .build();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreateDto(
            @JsonProperty("first_name") String firstName,

            @JsonProperty("last_name") String lastName) {
    }
}
