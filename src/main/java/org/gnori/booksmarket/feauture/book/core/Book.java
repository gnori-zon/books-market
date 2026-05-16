package org.gnori.booksmarket.feauture.book.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.gnori.booksmarket.core.Requires;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.feauture.author.Author;
import org.gnori.booksmarket.feauture.genre.Genre;
import org.gnori.booksmarket.feauture.publisher.Publisher;

@Builder
public record Book(
        UUID id,
        String name,
        String description,
        Language language,
        LocalDate releaseDate,
        PublisherInfo publisher,
        Collection<AuthorInfo> authors,
        Collection<GenreInfo> genres,
        String coverAttachmentId,
        String contentAttachmentId) {
    @Builder
    public record PublisherInfo(
            @NonNull UUID id,
            @NonNull String name) {
        public static PublisherInfo from(Publisher publisher) {
            return PublisherInfo.builder()
                    .id(publisher.id())
                    .name(publisher.name())
                    .build();
        }
    }

    @Builder
    public record AuthorInfo(
            @NonNull UUID id,
            @NonNull String firstName,
            @NonNull String lastName) {
        public static AuthorInfo from(Author author) {
            return AuthorInfo.builder()
                    .id(author.id())
                    .firstName(author.firstName())
                    .lastName(author.lastName())
                    .build();
        }
    }

    @Builder
    public record GenreInfo(
            @NonNull UUID id,
            @NonNull String name) {
        public static GenreInfo from(Genre genre) {
            return GenreInfo.builder()
                    .id(genre.id())
                    .name(genre.name())
                    .build();
        }
    }

    public boolean hasDifferences(Book.Update update) {
        for (final var item : update.updates()) {
            if (item.hasDifference(this)) {
                return true;
            }
        }
        return false;
    }

    public Book apply(
            Book.Update update,
            PublisherInfo newPublisher,
            Map<UUID, AuthorInfo> newAuthorsById,
            Map<UUID, GenreInfo> newGenresById) {
        var actualName = this.name;
        var actualDescription = this.description;
        var actualLanguage = this.language;
        var actualReleaseDate = this.releaseDate;
        var actualPublisher = this.publisher;
        var actualAuthors = new ArrayList<>(this.authors);
        var actualGenres = new ArrayList<>(this.genres);
        var actualCoverAttachmentId = this.coverAttachmentId;
        var actualContentAttachmentId = this.contentAttachmentId;
        for (final var item : update.updates()) {
            switch (item) {
                case Update.Item.Name(String value):
                    actualName = value;
                    break;
                case Update.Item.Description(String value):
                    actualDescription = value;
                    break;
                case Update.Item.Language(Language value):
                    actualLanguage = value;
                    break;
                case Update.Item.ReleaseDate(LocalDate value):
                    actualReleaseDate = value;
                    break;
                case Update.Item.Publisher(UUID publisherId):
                    Requires.requireEquals(publisherId, newPublisher.id());
                    actualPublisher = newPublisher;
                    break;
                case Update.Item.Authors(@NonNull Set<UUID> addAuthorIds, @NonNull Set<UUID> deleteAuthorIds):
                    actualAuthors.removeIf(author -> deleteAuthorIds.contains(author.id()));
                    addAuthorIds.stream()
                            .map(id -> Objects.requireNonNull(newAuthorsById.get(id)))
                            .forEach(actualAuthors::add);
                    break;
                case Update.Item.Genres(@NonNull Set<UUID> addGenreIds, @NonNull Set<UUID> deleteGenreIds):
                    actualGenres.removeIf(genre -> deleteGenreIds.contains(genre.id()));
                    addGenreIds.stream()
                            .map(id -> Objects.requireNonNull(newGenresById.get(id)))
                            .forEach(actualGenres::add);
                    break;
                case Update.Item.CoverAttachmentId(String value):
                    actualCoverAttachmentId = value;
                    break;
                case Update.Item.ContentAttachmentId(@NonNull String value):
                    actualContentAttachmentId = value;
                    break;
            }
        }
        return Book.builder()
                .id(this.id)
                .name(actualName)
                .description(actualDescription)
                .language(actualLanguage)
                .releaseDate(actualReleaseDate)
                .publisher(actualPublisher)
                .authors(actualAuthors)
                .genres(actualGenres)
                .coverAttachmentId(actualCoverAttachmentId)
                .contentAttachmentId(actualContentAttachmentId)
                .build();
    }

    @Builder
    public record Create(
            UUID id,
            String name,
            String description,
            Language language,
            LocalDate releaseDate,
            UUID publisherId,
            Set<UUID> authorIds,
            Set<UUID> genreIds,
            String coverAttachmentId,
            String contentAttachmentId) {
        public Create(
                @NonNull UUID id,
                @NonNull String name,
                @NonNull String description,
                @NonNull Language language,
                @NonNull LocalDate releaseDate,
                @NonNull UUID publisherId,
                @NonNull Set<UUID> authorIds,
                @NonNull Set<UUID> genreIds,
                String coverAttachmentId,
                @NonNull String contentAttachmentId) {
            this.id = id;
            this.name = name.trim();
            this.description = description.trim();
            this.language = language;
            this.releaseDate = releaseDate;
            this.publisherId = publisherId;
            this.authorIds = Set.copyOf(authorIds);
            this.genreIds = Set.copyOf(genreIds);
            this.coverAttachmentId = coverAttachmentId;
            this.contentAttachmentId = contentAttachmentId;
        }

        public Book toBook(
                @NonNull Publisher publisher,
                @NonNull Map<UUID, Author> authorsById,
                @NonNull Map<UUID, Genre> genresById) {
            Requires.requireEquals(this.publisherId, publisher.id());
            return Book.builder()
                    .id(this.id)
                    .name(this.name)
                    .description(this.description)
                    .language(this.language)
                    .releaseDate(this.releaseDate)
                    .publisher(PublisherInfo.from(publisher))
                    .authors(
                            this.authorIds().stream()
                                    .map(id -> Objects.requireNonNull(authorsById.get(id)))
                                    .map(AuthorInfo::from)
                                    .toList())
                    .genres(
                            this.genreIds().stream()
                                    .map(id -> Objects.requireNonNull(genresById.get(id)))
                                    .map(GenreInfo::from)
                                    .toList())
                    .coverAttachmentId(this.coverAttachmentId)
                    .contentAttachmentId(this.contentAttachmentId)
                    .build();
        }

        public void validate() {
            validateName(this.name);
            validateDescription(this.description);
            validateAuthorsSize(this.authorIds.size());
            validateGenresSize(this.genreIds.size());
        }
    }

    public static class Update {
        private final Map<Update.Field, Update.Item> updates;

        public Update(Iterable<Update.Item> updates) {
            this.updates = new HashMap<>();
            for (final Update.Item update : updates) {
                this.updates.put(update.field(), update);
            }
        }

        public Update(@NonNull Update.Item... updates) {
            this(() -> Arrays.stream(updates).iterator());
        }

        public boolean isEmpty() {
            return updates.isEmpty();
        }

        public Collection<Update.Item> updates() {
            return updates.values();
        }

        public void validate() {
            this.updates().forEach(Update.Item::validate);
        }

        public <I extends Update.Item> boolean contains(Class<I> itemClazz) {
            return updates.containsKey(Update.Field.from(itemClazz));
        }

        public <I extends Update.Item> I get(Class<I> itemClazz) {
            return itemClazz.cast(updates.get(Update.Field.from(itemClazz)));
        }

        public Update actualize(Book exist) {
            final List<Item> actualItems = new ArrayList<>();
            for (final var item : updates()) {
                if (item.hasDifference(exist)) {
                    actualItems.add(item);
                }
            }
            return new Update(actualItems);
        }

        public boolean containsAny(Iterable<? extends Class<? extends Item>> expectedItems) {
            for (var expectedItem : expectedItems) {
                if (contains(expectedItem)) {
                    return true;
                }
            }
            return false;
        }

        @Getter(AccessLevel.PRIVATE)
        @Accessors(fluent = true)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public enum Field {
            NAME(Update.Item.Name.class),
            DESCRIPTION(Update.Item.Description.class),
            PUBLISHER(Update.Item.Publisher.class),
            LANGUAGE(Update.Item.Language.class),
            RELEASE_DATE(Update.Item.ReleaseDate.class),
            AUTHORS(Update.Item.Authors.class),
            GENRES(Update.Item.Genres.class),
            COVER_ATTACHMENT_ID(Update.Item.CoverAttachmentId.class),
            CONTENT_ATTACHMENT_ID(Update.Item.ContentAttachmentId.class);

            public static final Map<Class<? extends Update.Item>, Update.Field> FIELD_BY_ITEM_CLASS = Arrays
                    .stream(values())
                    .collect(Collectors.toMap(Update.Field::itemClazz, Function.identity()));

            public static <I extends Update.Item> Update.Field from(Class<I> itemClazz) {
                return Objects.requireNonNull(FIELD_BY_ITEM_CLASS.get(itemClazz));
            }

            private final Class<? extends Update.Item> itemClazz;
        }

        public sealed interface Item permits
                Item.Name,
                Item.Description,
                Item.Genres,
                Item.Authors,
                Item.Publisher,
                Item.Language,
                Item.ReleaseDate,
                Item.CoverAttachmentId,
                Item.ContentAttachmentId {

            Update.Field field();

            void validate();

            boolean hasDifference(Book book);

            record Name(@NonNull String value) implements Update.Item {
                public Name(@NonNull String value) {
                    this.value = value.trim();
                }

                @Override
                public Update.Field field() {
                    return Update.Field.NAME;
                }

                @Override
                public void validate() {
                    validateName(this.value);
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !Objects.equals(this.value, book.name());
                }
            }

            record Description(@NonNull String value) implements Update.Item {
                public Description(@NonNull String value) {
                    this.value = value.trim();
                }

                @Override
                public Update.Field field() {
                    return Update.Field.DESCRIPTION;
                }

                @Override
                public void validate() {
                    validateDescription(this.value);
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !Objects.equals(this.value, book.description());
                }
            }

            record Genres(@NonNull Set<UUID> addGenreIds, @NonNull Set<UUID> deleteGenreIds) implements Update.Item {

                @Override
                public Field field() {
                    return Update.Field.GENRES;
                }

                @Override
                public void validate() {
                    validateMaxSizeOfGenres(addGenreIds.size());
                }

                @Override
                public boolean hasDifference(Book book) {
                    if (addGenreIds.isEmpty() && deleteGenreIds.isEmpty()) {
                        return false;
                    }
                    final Set<UUID> existGenreIds = book.genres().stream().map(GenreInfo::id)
                            .collect(Collectors.toSet());
                    return notHasAnyAddIdOrHasAnyDeletedId(existGenreIds, addGenreIds, deleteGenreIds);
                }
            }

            record Authors(@NonNull Set<UUID> addAuthorIds, @NonNull Set<UUID> deleteAuthorIds) implements Update.Item {

                @Override
                public Field field() {
                    return Update.Field.AUTHORS;
                }

                @Override
                public void validate() {
                    validateMaxSizeOfAuthors(addAuthorIds.size());
                }

                @Override
                public boolean hasDifference(Book book) {
                    if (addAuthorIds.isEmpty() && deleteAuthorIds.isEmpty()) {
                        return false;
                    }
                    final Set<UUID> existAuthorIds = book.authors().stream().map(AuthorInfo::id)
                            .collect(Collectors.toSet());
                    return notHasAnyAddIdOrHasAnyDeletedId(existAuthorIds, addAuthorIds, deleteAuthorIds);
                }
            }

            record Publisher(@NonNull UUID publisherId) implements Update.Item {
                @Override
                public Field field() {
                    return Update.Field.PUBLISHER;
                }

                @Override
                public void validate() {
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !publisherId.equals(book.publisher.id());
                }
            }

            record Language(@NonNull Book.Language value) implements Update.Item {

                @Override
                public Field field() {
                    return Update.Field.LANGUAGE;
                }

                @Override
                public void validate() {
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !value.equals(book.language());
                }
            }

            record ReleaseDate(@NonNull LocalDate value) implements Update.Item {

                @Override
                public Field field() {
                    return Update.Field.RELEASE_DATE;
                }

                @Override
                public void validate() {
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !value.equals(book.releaseDate());
                }
            }

            record CoverAttachmentId(String value) implements Update.Item {

                @Override
                public Field field() {
                    return Field.COVER_ATTACHMENT_ID;
                }

                @Override
                public void validate() {
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !Objects.equals(book.coverAttachmentId(), value);
                }
            }

            record ContentAttachmentId(@NonNull String value) implements Update.Item {

                @Override
                public Field field() {
                    return Field.CONTENT_ATTACHMENT_ID;
                }

                @Override
                public void validate() {
                }

                @Override
                public boolean hasDifference(Book book) {
                    return !Objects.equals(book.contentAttachmentId(), value);
                }
            }
        }

        private static boolean notHasAnyAddIdOrHasAnyDeletedId(Set<UUID> existsIds, Set<UUID> addIds,
                Set<UUID> deleteIds) {
            for (final var addId : addIds) {
                if (!existsIds.contains(addId)) {
                    return true;
                }
            }
            for (final var deleteId : deleteIds) {
                if (existsIds.contains(deleteId)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public enum Language {
        ZHO("Chinese"),
        ENG("English"),
        SPA("Spanish"),
        ARA("Arab"),
        FRA("French"),
        RUS("Russian"),
        POR("Portuguese"),
        DEU("German");

        private final String value;

        private static final Map<String, Language> LANGUAGE_BY_VALUE = Arrays.stream(Language.values())
                .collect(Collectors.toMap(Language::value, Function.identity()));

        public static Language fromString(String value) {
            if (value == null) {
                return null;
            }
            return LANGUAGE_BY_VALUE.get(value);
        }
    }

    private static void validateName(String value) {
        if (value.isBlank() || value.length() > 255) {
            throw BaseAppException.invalidFieldLength("name", 1, 255);
        }
    }

    private static void validateDescription(String value) {
        if (value.isBlank() || value.length() > 32_767) {
            throw BaseAppException.invalidFieldLength("description", 1, 32_767);
        }
    }

    public static void validateGenresSize(int genresSize) {
        validateMinSizeOfGenres(genresSize);
        validateMaxSizeOfGenres(genresSize);
    }

    public static void validateAuthorsSize(int authorsSize) {
        validateMinSizeOfAuthors(authorsSize);
        validateMaxSizeOfAuthors(authorsSize);
    }

    private static void validateMinSizeOfAuthors(int authorsSize) {
        if (authorsSize < 1) {
            throw BaseAppException.invalidFieldSize("authors", 1, 10);
        }
    }

    private static void validateMaxSizeOfAuthors(int authorsSize) {
        if (authorsSize > 10) {
            throw BaseAppException.invalidFieldSize("authors", 1, 10);
        }
    }

    private static void validateMinSizeOfGenres(int genresSize) {
        if (genresSize < 1) {
            throw BaseAppException.invalidFieldSize("genres", 1, 10);
        }
    }

    private static void validateMaxSizeOfGenres(int genresSize) {
        if (genresSize > 10) {
            throw BaseAppException.invalidFieldSize("genres", 1, 10);
        }
    }
}
