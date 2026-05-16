package org.gnori.booksmarket.feauture.book.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.gnori.booksmarket.core.EntityType;
import org.gnori.booksmarket.core.db.Constraint;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.core.exception.Catchers;
import org.gnori.booksmarket.core.exception.Extractors;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate jdbc;

    private static final String FIND_BY_ID = """
            SELECT
                b.id,
                b.name,
                b.description,
                b.language,
                b.release_date,

                jsonb_build_object(
                    'id', p.id,
                    'name', p.name
                ) AS publisher,

                (
                    SELECT
                        jsonb_agg(
                            jsonb_build_object(
                                'id', a.id,
                                'firstName', a.first_name,
                                'lastName', a.last_name
                            )
                        )
                    FROM book_authors ba
                        JOIN authors a ON a.id = ba.author_id
                    WHERE ba.book_id = b.id
                ) AS authors,

                (
                    SELECT
                        jsonb_agg(
                            jsonb_build_object(
                                'id', g.id,
                                'name', g.name
                            )
                        )
                    FROM book_genres bg
                        JOIN genres g ON g.id = bg.genre_id
                    WHERE bg.book_id = b.id
                ) AS genres,

                b.cover_attachment_id,
                b.content_attachment_id
            FROM books b
                JOIN publishers p ON p.id = b.publisher_id
            WHERE b.id = :id
            """.stripIndent();

    @Override
    public Optional<Book> findById(UUID id) {
        final List<Book> result = jdbc.query(FIND_BY_ID, Map.ofEntries(Map.entry("id", id)), this::parseBook);
        return result.isEmpty()
                ? Optional.empty()
                : Optional.of(result.getFirst());
    }

    private static final String FIND_BY_ID_FOR_UPDATE = FIND_BY_ID + " \nFOR UPDATE";

    @Override
    public Optional<Book> findByIdForUpdate(UUID id) {
        final List<Book> result = jdbc.query(FIND_BY_ID_FOR_UPDATE, Map.ofEntries(Map.entry("id", id)),
                this::parseBook);
        return result.isEmpty()
                ? Optional.empty()
                : Optional.of(result.getFirst());
    }

    private static final String CREATE_BOOKS_QUERY = """
            INSERT INTO books (id, name, description, language, release_date, publisher_id, cover_attachment_id, content_attachment_id)
                VALUES(:id, :name, :description, :language, :releaseDate, :publisherId, :coverAttachmentId, :contentAttachmentId)
            """
            .stripIndent();

    private static final String CREATE_BOOK_AUTHORS_QUERY = """
                INSERT INTO book_authors (book_id, author_id)
                    VALUES (:bookId, :authorId)
            """.stripIndent();

    private static final String CREATE_BOOK_GENRES_QUERY = """
                INSERT INTO book_genres (book_id, genre_id)
                    VALUES (:bookId, :genreId)
            """.stripIndent();

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void create(Book.Create create) {
        substituteException(() -> {
            final var params = new java.util.HashMap<String, Object>();
            params.put("id", create.id());
            params.put("name", create.name());
            params.put("description", create.description());
            params.put("language", create.language().value());
            params.put("releaseDate", create.releaseDate());
            params.put("publisherId", create.publisherId());
            params.put("coverAttachmentId", create.coverAttachmentId());
            params.put("contentAttachmentId", create.contentAttachmentId());
            jdbc.update(CREATE_BOOKS_QUERY, params);

            jdbc.batchUpdate(CREATE_BOOK_AUTHORS_QUERY,
                    toBatchParameters(create.id(), "authorId", create.authorIds()));

            jdbc.batchUpdate(CREATE_BOOK_GENRES_QUERY,
                    toBatchParameters(create.id(), "genreId", create.genreIds()));
        });
    }

    public static final String DELETE_BOOK_GENRES_BY_IDS = """
            DELETE FROM book_genres
            WHERE book_id = :book_id AND genre_id IN (:genre_ids)
            """.stripIndent();

    public static final String DELETE_BOOK_AUTHORS_BY_IDS = """
            DELETE FROM book_authors
            WHERE book_id = :book_id AND author_id IN (:author_ids)
            """.stripIndent();

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean updateById(UUID id, Book.Update update) {
        return substituteException(() -> {
            final Optional<Book> exist = findByIdForUpdate(id);
            if (exist.isEmpty()) {
                return false;
            }
            updateBookIfNeeded(id, update);
            updateGenresIfNeeded(id, update);
            updateAuthorsIfNeeded(id, update);
            return true;
        });
    }

    private static final Set<? extends Class<? extends Book.Update.Item>> MAIN_TABLE_UPDATES = Set.of(
            Book.Update.Item.Name.class,
            Book.Update.Item.Description.class,
            Book.Update.Item.Language.class,
            Book.Update.Item.ReleaseDate.class,
            Book.Update.Item.Publisher.class);

    private void updateBookIfNeeded(UUID id, Book.Update update) {
        if (update.containsAny(MAIN_TABLE_UPDATES)) {
            final var queryWithParams = buildUpdateQuery(id, update);
            jdbc.update(queryWithParams.getLeft(), queryWithParams.getRight());
        }
    }

    private void updateGenresIfNeeded(UUID id, Book.Update update) {
        if (!update.contains(Book.Update.Item.Genres.class)) {
            return;
        }
        final var updateItem = update.get(Book.Update.Item.Genres.class);
        if (!updateItem.deleteGenreIds().isEmpty()) {
            jdbc.update(DELETE_BOOK_GENRES_BY_IDS,
                    Map.ofEntries(
                            Map.entry("book_id", id),
                            Map.entry("genre_ids", updateItem.deleteGenreIds())));
        }
        if (!updateItem.addGenreIds().isEmpty()) {
            jdbc.batchUpdate(CREATE_BOOK_GENRES_QUERY,
                    toBatchParameters(id, "genreId", updateItem.addGenreIds()));
        }
    }

    private void updateAuthorsIfNeeded(UUID id, Book.Update update) {
        if (!update.contains(Book.Update.Item.Authors.class)) {
            return;
        }
        final var updateItem = update.get(Book.Update.Item.Authors.class);
        if (!updateItem.deleteAuthorIds().isEmpty()) {
            jdbc.update(DELETE_BOOK_AUTHORS_BY_IDS,
                    Map.ofEntries(
                            Map.entry("book_id", id),
                            Map.entry("author_ids", updateItem.deleteAuthorIds())));
        }
        if (!updateItem.addAuthorIds().isEmpty()) {
            jdbc.batchUpdate(CREATE_BOOK_AUTHORS_QUERY,
                    toBatchParameters(id, "authorId", updateItem.addAuthorIds()));
        }
    }

    private static final String DELETE_BOOK_BY_ID = """
            DELETE FROM books
            WHERE id = :id
            """.stripIndent();

    @Override
    public boolean deleteById(UUID id) {
        int countRowAffected = jdbc.update(DELETE_BOOK_BY_ID, Map.ofEntries(Map.entry("id", id)));
        return countRowAffected == 1;
    }

    private Pair<String, Map<String, Object>> buildUpdateQuery(@NonNull UUID id, @NonNull Book.Update update) {
        if (update.isEmpty()) {
            throw new IllegalArgumentException("Update must not be empty");
        }

        final List<String> sqlUpdates = new ArrayList<>();
        final var args = new HashMap<String, Object>();
        args.put("id", id);
        for (final var item : update.updates()) {
            switch (item) {
                case Book.Update.Item.Name(String value):
                    sqlUpdates.add("name = :name");
                    args.put("name", value);
                    break;
                case Book.Update.Item.Description(String value):
                    sqlUpdates.add("description = :description");
                    args.put("description", value);
                    break;
                case Book.Update.Item.Language(Book.Language value):
                    sqlUpdates.add("language = :language");
                    args.put("language", value.value());
                    break;
                case Book.Update.Item.ReleaseDate(LocalDate value):
                    sqlUpdates.add("release_date = :releaseDate");
                    args.put("releaseDate", value);
                    break;
                case Book.Update.Item.Publisher(UUID publisherId):
                    sqlUpdates.add("publisher_id = :publisherId");
                    args.put("publisherId", publisherId);
                    break;
                case Book.Update.Item.CoverAttachmentId(String value):
                    sqlUpdates.add("cover_attachment_id = :coverAttachmentId");
                    args.put("coverAttachmentId", value);
                    break;
                case Book.Update.Item.ContentAttachmentId(String value):
                    sqlUpdates.add("content_attachment_id = :contentAttachmentId");
                    args.put("contentAttachmentId", value);
                    break;
                case Book.Update.Item.Genres ignored:
                    break;
                case Book.Update.Item.Authors ignored:
                    break;
            }
        }
        final var query = String.join(
                " ",
                List.of(
                        "UPDATE books SET",
                        String.join(" , ", sqlUpdates),
                        "WHERE id = :id"));
        return Pair.of(query, args);
    }

    private static SqlParameterSource[] toBatchParameters(UUID bookId, String refParam, Set<UUID> refIds) {
        return refIds
                .stream()
                .map(refId -> new MapSqlParameterSource()
                        .addValue("bookId", bookId)
                        .addValue(refParam, refId))
                .toArray(SqlParameterSource[]::new);
    }

    private Book parseBook(ResultSet rs, int i) throws SQLException {
        try {
            return Book.builder()
                    .id(rs.getObject("id", UUID.class))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .language(Book.Language.fromString(rs.getString("language")))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .publisher(parsePublisher(rs.getString("publisher")))
                    .authors(parseAuthors(rs.getString("authors")))
                    .genres(parseGenres(rs.getString("genres")))
                    .coverAttachmentId(rs.getString("cover_attachment_id"))
                    .contentAttachmentId(rs.getString("content_attachment_id"))
                    .build();
        } catch (JsonProcessingException e) {
            throw BaseAppException.unexpected(e, log);
        }
    }

    private Book.PublisherInfo parsePublisher(String publisherJson) throws JsonProcessingException {
        Objects.requireNonNull(publisherJson);
        final var publisherDto = objectMapper.readValue(publisherJson, PublisherInfoDto.class);
        return Book.PublisherInfo.builder()
                .id(publisherDto.id())
                .name(publisherDto.name())
                .build();
    }

    private Collection<Book.AuthorInfo> parseAuthors(String authorsJson) throws JsonProcessingException {
        Objects.requireNonNull(authorsJson);
        final var authorsDto = objectMapper.readValue(authorsJson, new TypeReference<List<AuthorInfoDto>>() {
        });
        return authorsDto.stream()
                .map(authorDto -> Book.AuthorInfo.builder()
                        .id(authorDto.id())
                        .firstName(authorDto.firstName())
                        .lastName(authorDto.lastName())
                        .build())
                .toList();
    }

    private Collection<Book.GenreInfo> parseGenres(String genresJson) throws JsonProcessingException {
        Objects.requireNonNull(genresJson);
        final var genresDto = objectMapper.readValue(genresJson, new TypeReference<List<GenreInfoDto>>() {
        });
        return genresDto.stream()
                .map(genreDto -> Book.GenreInfo.builder()
                        .id(genreDto.id())
                        .name(genreDto.name())
                        .build())
                .toList();
    }

    private void substituteException(Runnable runnable) {
        substituteException(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T substituteException(Supplier<T> supplier) {
        return Catchers.substituteException(supplier::get, DataIntegrityViolationException.class, e -> {
            final var psqlExceptionOptional = Extractors.tryExtract(e, PSQLException.class);
            if (psqlExceptionOptional.isEmpty() || psqlExceptionOptional.get().getServerErrorMessage() == null) {
                throw e;
            }
            final PSQLException psqlException = psqlExceptionOptional.get();
            final var constraintOptional = Constraint.fromSqlStateOrEmpty(psqlException.getSQLState());
            if (constraintOptional.isEmpty()) {
                throw e;
            }
            final String constraintName = psqlException.getServerErrorMessage().getConstraint();
            throw switch (constraintOptional.get()) {
                case Constraint.UNIQUE -> {
                    if ("unique_book_author_pair".equals(constraintName)) {
                        yield BookExceptions.bookAlreadyHasSameAuthors();
                    }
                    if ("unique_book_genre_pair".equals(constraintName)) {
                        yield BookExceptions.bookAlreadyHasSameGenres();
                    }
                    yield e;
                }
                case Constraint.FK -> {
                    final String detail = psqlException.getServerErrorMessage().getDetail();
                    final var id = tryExtractId(detail);
                    if (id == null) {
                        yield e;
                    }
                    if ("books_publisher_id_fkey".equals(constraintName)) {
                        yield BaseAppException.notFoundById(EntityType.PUBLISHER, id);
                    }
                    if ("book_genres_genre_id_fkey".equals(constraintName)) {
                        yield BaseAppException.notFoundById(EntityType.GENRE, id);
                    }
                    if ("book_authors_author_id_fkey".equals(constraintName)) {
                        yield BaseAppException.notFoundById(EntityType.AUTHOR, id);
                    }
                    yield e;
                }
                default -> e;
            };
        });
    }

    private static final Pattern FKEY_ID_PATTERN = Pattern.compile("\\((.*?)\\)=\\((.*?)\\)");

    private static UUID tryExtractId(String detail) {
        try {
            final Matcher matcher = FKEY_ID_PATTERN.matcher(detail);
            return matcher.find()
                    ? UUID.fromString(matcher.group(2))
                    : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private record PublisherInfoDto(
            @NonNull UUID id,
            @NonNull String name) {
    }

    private record AuthorInfoDto(
            @NonNull UUID id,
            @NonNull String firstName,
            @NonNull String lastName) {
    }

    private record GenreInfoDto(
            @NonNull UUID id,
            @NonNull String name) {
    }
}
