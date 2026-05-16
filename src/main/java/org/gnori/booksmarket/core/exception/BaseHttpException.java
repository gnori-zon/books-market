package org.gnori.booksmarket.core.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseHttpException {

    public static HttpException invalidField(String fieldName, String fieldValue) {
        return new HttpException(() -> format("invalid field '%s': %s", fieldName, fieldValue), HttpStatus.BAD_REQUEST);
    }

    public static <T> T requireNotNull(String fieldName, T fieldValue) {
        if (fieldValue != null) {
            return fieldValue;
        }
        throw new HttpException(() -> format("field '%s' must be filled", fieldName), HttpStatus.BAD_REQUEST);
    }
}
