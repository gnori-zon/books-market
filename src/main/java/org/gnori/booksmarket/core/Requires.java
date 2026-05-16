package org.gnori.booksmarket.core;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Requires {

    private static final String DEFAULT_REQUIRE_EQUALS_MESSAGE = "params are not equal";

    public static <T> void requireEquals(T lhs, T rhs) {
        requireEquals(lhs, rhs, DEFAULT_REQUIRE_EQUALS_MESSAGE);
    }

    public static <T> void requireEquals(T lhs, T rhs, String message) {
        if (!Objects.equals(lhs, rhs)) {
            throw new IllegalArgumentException(message);
        }
    }
}
