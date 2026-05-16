package org.gnori.booksmarket.feauture.book.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.gnori.booksmarket.core.exception.AppException;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookExceptions {

    @Getter
    @Accessors(fluent = true)
    @AllArgsConstructor
    public enum Type implements AppException.Type.Base {
        BOOK_ALREADY_HAS_SAME_ATHORS("bookAlreadyHasSameAuthors", HttpStatus.CONFLICT),
        BOOK_ALREADY_HAS_SAME_GENRES("bookAlreadyHasSameGenres", HttpStatus.CONFLICT);

        private final String key;
        private final HttpStatus status;

        private static final String BOOK_EXCEPTION_PATTERNS_BUNDLE = "BookExceptionPatterns";

        @Override
        public String bundle() {
            return BOOK_EXCEPTION_PATTERNS_BUNDLE;
        }
    }

    public static AppException bookAlreadyHasSameAuthors() {
        return new AppException(Type.BOOK_ALREADY_HAS_SAME_ATHORS, new Object[0]);
    }

    public static AppException bookAlreadyHasSameGenres() {
        return new AppException(Type.BOOK_ALREADY_HAS_SAME_GENRES, new Object[0]);
    }

}
