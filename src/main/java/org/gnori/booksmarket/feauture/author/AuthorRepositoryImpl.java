package org.gnori.booksmarket.feauture.author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthorRepositoryImpl implements AuthorRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private static final String FIND_BY_ID_QUERY = """
        SELECT id, first_name, last_name
        FROM authors
        WHERE id = :id
        LIMIT 1
        """.stripIndent();

    @Override
    public Optional<Author> findById(@NonNull UUID id) {
        final var result = jdbc.query(FIND_BY_ID_QUERY, Map.ofEntries(Map.entry("id", id)), this::parseAuthor);
        return result.isEmpty()
            ? Optional.empty()
            : Optional.of(result.getFirst());
    }

    private static final String CREATE_QUERY = """
        INSERT INTO authors (id, first_name, last_name)
            VALUES (:id, :firstName, :lastName)
        """.stripIndent();

    @Override
    public void create(Author.Create create) {
        jdbc.update(CREATE_QUERY,
            Map.ofEntries(
                Map.entry("id", create.id()),
                Map.entry("firstName", create.firstName()),
                Map.entry("lastName", create.lastName())
            )
        );
    }

    @Override
    public boolean updateById(UUID id, Author.Update update) {
        if (update.isEmpty()) {
            return findById(id).isPresent();
        }
        final var queryWithParams = buildUpdateQuery(id, update);
        final int countRowAffected = jdbc.update(queryWithParams.getLeft(), queryWithParams.getRight());
        return countRowAffected == 1;
    }

    private static final String DELETE_BY_ID_QUERY = """
        DELETE FROM authors
        WHERE id = :id
        """.stripIndent();

    @Override
    public boolean deleteById(UUID id) {
        final int countRowAffected = jdbc.update(DELETE_BY_ID_QUERY, Map.ofEntries(Map.entry("id", id)));
        return countRowAffected == 1;
    }


    private static final String FIND_BY_ID_IN_QUERY = """
        SELECT id, first_name, last_name
        FROM authors
        WHERE id IN (:ids)
        """.stripIndent();

    @Override
    public List<Author> findByIdIn(@NonNull Set<UUID> ids) {
        return jdbc.query(FIND_BY_ID_IN_QUERY, Map.ofEntries(Map.entry("ids", ids)), this::parseAuthor);
    }

    private Pair<String, Map<String, Object>> buildUpdateQuery(@NonNull UUID id, @NonNull Author.Update update) {
        if (update.isEmpty()) {
            throw new IllegalArgumentException("Update must not be empty");
        }

        final List<String> sqlUpdates = new ArrayList<>();
        final var args = new HashMap<String, Object>();
        args.put("id", id);
        for (final var item: update.updates()) {
            switch (item) {
                case Author.Update.Item.FirstName(String value):
                    sqlUpdates.add("first_name = :firstName");
                    args.put("firstName", value);
                    break;
                case Author.Update.Item.LastName(String value):
                    sqlUpdates.add("last_name = :lastName");
                    args.put("lastName", value);
                    break;
            }
        }
        final var query = String.join(
            " ",
            List.of(
                "UPDATE authors SET",
                String.join(" , ", sqlUpdates),
                "WHERE id = :id"
            )
        );
        return Pair.of(query, args);
    }

    private Author parseAuthor(ResultSet rs, int i) throws SQLException {
        return Author.builder()
            .id(rs.getObject("id", UUID.class))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .build();
    }
}
