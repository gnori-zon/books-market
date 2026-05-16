package org.gnori.booksmarket.feauture.book.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("language") LanguageDto language,
        @JsonProperty("releaseDate") LocalDate releaseDate,
        @JsonProperty("publisher") PublisherInfoDto publisher,
        @JsonProperty("genres") Map<UUID, GenreInfoDto> genres,
        @JsonProperty("authors") Map<UUID, AuthorInfoDto> authors,
        @JsonProperty("coverAttachmentId") String coverAttachmentId,
        @JsonProperty("contentAttachmentId") String contentAttachmentId) {
    @Builder
    public record LanguageDto(
            @JsonProperty("value") String value,
            @JsonProperty("displayName") String displayName) {
    }

    @Builder
    public record PublisherInfoDto(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name) {
    }

    @Builder
    public record GenreInfoDto(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name) {
    }

    @Builder
    public record AuthorInfoDto(
            @JsonProperty("id") UUID id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName) {
    }

    public static BookDto from(Book book) {
        return BookDto.builder()
                .id(book.id())
                .name(book.name())
                .description(book.description())
                .language(
                        LanguageDto.builder()
                                .value(book.language().value())
                                .displayName(book.language().value())
                                .build())
                .releaseDate(book.releaseDate())
                .publisher(
                        PublisherInfoDto.builder()
                                .id(book.publisher().id())
                                .name(book.publisher().name())
                                .build())
                .authors(
                        book.authors().stream()
                                .map(author -> AuthorInfoDto.builder()
                                        .id(author.id())
                                        .firstName(author.firstName())
                                        .lastName(author.lastName())
                                        .build())
                                .collect(Collectors.toMap(AuthorInfoDto::id, Function.identity())))
                .genres(
                        book.genres().stream()
                                .map(genre -> GenreInfoDto.builder()
                                        .id(genre.id())
                                        .name(genre.name())
                                        .build())
                                .collect(Collectors.toMap(GenreInfoDto::id, Function.identity())))
                .coverAttachmentId(book.coverAttachmentId())
                .contentAttachmentId(book.contentAttachmentId())
                .build();
    }

    public record CreateDto(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("language") String language,
            @JsonProperty("releaseDate") LocalDate releaseDate,
            @JsonProperty("publisherId") UUID publisherId,
            @JsonProperty("authorIds") Set<UUID> authorIds,
            @JsonProperty("genreIds") Set<UUID> genreIds,
            @JsonProperty("coverAttachmentId") String coverAttachmentId,
            @JsonProperty("contentAttachmentId") String contentAttachmentId) {
    }
}
