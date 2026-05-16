package org.gnori.booksmarket.core.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.gnori.booksmarket.core.EntityType;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseAppException {

    public static Collector<AppException, ? extends List<AppException>, AppExceptionList> toListCollector() {
        return Collector.of(
                ArrayList::new,
                ArrayList::add,
                (lhs, rhs) -> {
                    lhs.addAll(rhs);
                    return lhs;
                },
                AppExceptionList::new);
    }

    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    public enum Type implements AppException.Type.Base {
        UNEXPECTED("unexpected", HttpStatus.INTERNAL_SERVER_ERROR),
        NOT_FOUND_BY_FIELD("notFoundByField", HttpStatus.NOT_FOUND),
        INVALID_FIELD_LENGTH("invalidFieldLength", HttpStatus.BAD_REQUEST),
        INVALID_FIELD_SIZE("invalidFieldSize", HttpStatus.BAD_REQUEST),
        UNIQUE_CONSTRAINT_VIOLATION("uniqueConstraintViolation", HttpStatus.CONFLICT);

        private static final String BASE_APP_EXCEPTION_PATTERNS_BUNDLE = "BaseAppExceptionPatterns";
        private final String key;
        private final HttpStatus status;

        @Override
        public String bundle() {
            return BASE_APP_EXCEPTION_PATTERNS_BUNDLE;
        }
    }

    public static AppExceptionList listOf(@NonNull AppException... exceptions) {
        return new AppExceptionList(Arrays.stream(exceptions).toList());
    }

    public static AppException notFoundFile(@NonNull String id) {
        return new AppException(Type.NOT_FOUND_BY_FIELD, new Object[] { "file", id });
    }

    public static AppException notFoundById(EntityType type, UUID id) {
        return new AppException(Type.NOT_FOUND_BY_FIELD, new Object[] { type.value(), id });
    }

    public static AppException invalidFieldLength(String field, int minLengthInclusive, int maxLengthInclusive) {
        return new AppException(Type.INVALID_FIELD_LENGTH,
                new Object[] { field, minLengthInclusive, maxLengthInclusive });
    }

    public static AppException invalidFieldSize(String field, int minLengthInclusive, int maxLengthInclusive) {
        return new AppException(Type.INVALID_FIELD_SIZE,
                new Object[] { field, minLengthInclusive, maxLengthInclusive });
    }

    public static AppException uniqueConstraintViolation(String... fields) {
        return new AppException(Type.UNIQUE_CONSTRAINT_VIOLATION, new Object[] { String.join(",", fields) });
    }

    public static AppException unexpected(Throwable cause, Logger logger) {
        return unexpected(cause, logger::error);
    }

    public static AppException unexpected(Throwable cause, BiConsumer<String, Throwable> logger) {
        final var exception = new AppException(Type.UNEXPECTED, new Object[] { UUID.randomUUID() });
        logger.accept(exception.getMessage(), cause);
        return exception;
    }
}
