package org.gnori.booksmarket.core.db;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum Constraint {
    UNIQUE("23505"),
    CHECK("23514"),
    FK("23503");

    public final String sqlState;

    private static final Map<String, Constraint> CONSTRAINT_BY_SQL_STATE = Arrays.stream(values())
        .collect(Collectors.toMap(Constraint::sqlState, Function.identity()));

    public static Optional<Constraint> fromSqlStateOrEmpty(String sqlState) {
        return Optional.ofNullable(CONSTRAINT_BY_SQL_STATE.get(sqlState));
    }
}
