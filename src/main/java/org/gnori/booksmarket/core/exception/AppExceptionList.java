package org.gnori.booksmarket.core.exception;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;

public class AppExceptionList extends RuntimeException {
    private final List<AppException> exceptions;

    public AppExceptionList(@NonNull List<AppException> exceptions) {
        if (exceptions.isEmpty()) {
            throw new IllegalArgumentException("exception can't be empty list");
        }
        this.exceptions = List.copyOf(exceptions);
    }

    @Override
    public String getMessage() {
        return exceptions.stream().map(AppException::getMessage).collect(Collectors.joining(";"));
    }

    public int size() {
        return exceptions.size();
    }

    public AppException get(int index) {
        return exceptions.get(index);
    }

    public AppException first() {
        return exceptions.get(0);
    }

    public <T> List<T> map(Function<AppException, T> mapper) {
        return exceptions.stream().map(mapper).toList();
    }
}
