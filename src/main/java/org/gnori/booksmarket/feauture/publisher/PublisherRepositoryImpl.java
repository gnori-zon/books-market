package org.gnori.booksmarket.feauture.publisher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.gnori.booksmarket.core.db.Constraint;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.core.exception.Catchers;
import org.gnori.booksmarket.core.exception.Extractors;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PublisherRepositoryImpl implements PublisherRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private static final String FIND_BY_ID_QUERY = """
        SELECT id, name
        FROM publishers
        WHERE id = :id
        """.stripIndent();

    @Override
    public Optional<Publisher> findById(UUID id) {
        final List<Publisher> result = jdbc.query(FIND_BY_ID_QUERY, Map.ofEntries(Map.entry("id", id)), this::parsePublisher);
        return result.isEmpty()
            ? Optional.empty()
            : Optional.of(result.getFirst());
    }

    private static final String CREATE_QUERY = """
        INSERT INTO publishers (id, name)
            VALUES (:id, :name)
        """.stripIndent();

    @Override
    public void create(Publisher.Create create) {
        substituteException(() ->
            jdbc.update(CREATE_QUERY,
                Map.ofEntries(
                    Map.entry("id", create.id()),
                    Map.entry("name", create.name()))
            )
        );
    }

    @Override
    public boolean updateById(UUID id, Publisher.Update update) {
        return substituteException(() -> {
            if (update.isEmpty()) {
                return findById(id).isPresent();
            }
            final var queryWithParams = buildUpdateQuery(id, update);
            final int countRowAffected = jdbc.update(queryWithParams.getLeft(), queryWithParams.getRight());
            return countRowAffected == 1;
        });
    }

    private static final String DELETE_BY_ID_QUERY = """
        DELETE FROM publishers
        WHERE id = :id
        """.stripIndent();

    @Override
    public boolean deleteById(UUID id) {
        final int countRowAffected = jdbc.update(DELETE_BY_ID_QUERY, Map.ofEntries(Map.entry("id", id)));
        return countRowAffected == 1;
    }

    private static final String FIND_BY_NAME_QUERY = """
        SELECT id, name
        FROM publishers
        WHERE name = :name
        """.stripIndent();

    @Override
    public Optional<Object> findByName(@NonNull String name) {
        final List<Publisher> result = jdbc.query(FIND_BY_NAME_QUERY, Map.ofEntries(Map.entry("name", name)), this::parsePublisher);
        return result.isEmpty()
            ? Optional.empty()
            : Optional.of(result.getFirst());
    }

    private <T> T substituteException(Supplier<T> supplier) {
        return Catchers.substituteException(supplier::get, DuplicateKeyException.class, e -> {
            throw isUniqueLowerNameViolation(e) ? BaseAppException.uniqueConstraintViolation("name") : e;
        });
    }

    private Pair<String, Map<String, Object>> buildUpdateQuery(@NonNull UUID id, @NonNull Publisher.Update update) {
        if (update.isEmpty()) {
            throw new IllegalArgumentException("Update must not be empty");
        }

        final List<String> sqlUpdates = new ArrayList<>();
        final var args = new HashMap<String, Object>();
        args.put("id", id);
        for (final var item: update.updates()) {
            switch (item) {
                case Publisher.Update.Item.Name(String value):
                    sqlUpdates.add("name = :name");
                    args.put("name", value);
                    break;
            }
        }
        final var query = String.join(
            " ",
            List.of(
                "UPDATE publishers SET",
                String.join(" , ", sqlUpdates),
                "WHERE id = :id"
            )
        );
        return Pair.of(query, args);
    }

    private Publisher parsePublisher(ResultSet rs, int i) throws SQLException {
        return Publisher.builder()
            .id(rs.getObject("id", UUID.class))
            .name(rs.getString("name"))
            .build();
    }

    private boolean isUniqueLowerNameViolation(Exception exception) {
        final var psqlExceptionOptional = Extractors.tryExtract(exception, PSQLException.class);
        if (psqlExceptionOptional.isEmpty()) {
            return false;
        }
        final var psqlException = psqlExceptionOptional.get();
        return Constraint.UNIQUE.sqlState.equals(psqlException.getSQLState())
               && psqlException.getServerErrorMessage() != null
               && "unique_publisher_lower_name_idx".equals(psqlException.getServerErrorMessage().getConstraint());
    }
}
