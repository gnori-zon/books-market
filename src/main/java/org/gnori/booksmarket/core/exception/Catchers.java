package org.gnori.booksmarket.core.exception;

import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Catchers {

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Throwable;

        default Function<T, R> unchecked() {
            return t -> {
                try {
                    return this.apply(t);
                } catch (Throwable e) {
                    return sneakyThrow(e);
                }
            };
        };
    }

    public static <T, E extends Throwable> T substituteException(
        @NonNull CheckedSupplier<T> supplier,
        @NonNull Class<E> clazz,
        @NonNull CheckedFunction<E, T> catcher
    ) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            if (clazz.isInstance(e)) {
                return catcher.unchecked().apply(clazz.cast(e));
            }
            return sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, R> R sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
