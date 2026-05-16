package org.gnori.booksmarket.core.exception;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.http.HttpStatus;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppException extends RuntimeException {

    public interface Type {
        String pattern(Locale locale);

        int statusCode();

        interface Base extends Type {

            String key();

            String bundle();

            HttpStatus status();

            @Override
            default int statusCode() {
                return status().value();
            }

            @Override
            default String pattern(Locale locale) {
                try {
                    return ResourceBundle.getBundle(bundle(), locale).getString(key());
                } catch (MissingResourceException e) {
                    log.error("Not found pattern for exception'{}'.", key(), e);
                    throw e;
                }
            }
        }
    }

    private final Type type;
    private final Throwable cause;
    private final Object[] args;

    public AppException(
            @NonNull Type type,
            @NonNull Object[] args) {
        this(type, null, args);
    }

    public AppException(
            @NonNull Type type,
            Throwable cause,
            @NonNull Object[] args) {
        this.type = type;
        this.cause = cause;
        this.args = args;
    }

    public boolean is(Type type) {
        return this.type.equals(type);
    }

    @Override
    public String getMessage() {
        return MessageFormat.format(this.type.pattern(Locale.ENGLISH), args);
    }

    public HttpException toHttpException() {
        return new HttpException(this::getMessage, type.statusCode());
    }
}
