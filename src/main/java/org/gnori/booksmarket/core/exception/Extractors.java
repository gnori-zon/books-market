package org.gnori.booksmarket.core.exception;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Extractors {

    public static <E> Optional<E> tryExtract(Throwable from, Class<E> clazz) {
        Throwable cause = from;
        final Set<Throwable> visited = new HashSet<>();
        while (cause != null) {
            if (visited.contains(cause)) {
                return Optional.empty();
            }
            if (clazz.isInstance(cause)) {
                return Optional.of(clazz.cast(cause));
            }
            visited.add(cause);
            cause = cause.getCause();
        }
        return Optional.empty();
    }
}
