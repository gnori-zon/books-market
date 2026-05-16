package org.gnori.booksmarket.core.exception;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class HttpException extends RuntimeException {
    private final Supplier<String> messageSupplier;
    private final HttpStatus status;

    public HttpException(Supplier<String> messageSupplier, int status) {
        this.messageSupplier = messageSupplier;
        this.status = HttpStatus.valueOf(status);
    }

    @Override
    public String getMessage() {
        return messageSupplier.get();
    }

    public HttpStatus status() {
        return status;
    }
}
