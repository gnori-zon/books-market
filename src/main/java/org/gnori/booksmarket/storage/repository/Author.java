package org.gnori.booksmarket.storage.repository;

import java.util.UUID;

public record Author(
    UUID id,
    String firstName,
    String lastName
) {

    public record Create(
        UUID id,
        String firstName,
        String lastName
    ) {
    }

    public record Update(
    ) {

    }
}
