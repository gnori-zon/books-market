package org.gnori.booksmarket.feauture.author;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;
import org.gnori.booksmarket.core.exception.BaseAppException;

@Builder
public record Author(
    UUID id,
    String firstName,
    String lastName
) {

    public boolean hasDifferences(Update update) {
        for (final var item : update.updates()) {
            if (item.hasDifference(this)) {
                return true;
            }
        }
        return false;
    }

    public Author apply(Update update) {
        var newFirstName = this.firstName;
        var newLastName = this.firstName;
        for (final var item : update.updates()) {
            switch (item) {
                case Update.Item.FirstName(String value):
                    newFirstName = value;
                    break;
                case Update.Item.LastName(String value):
                    newLastName = value;
                    break;
            }
        }
        return Author.builder()
            .id(this.id)
            .firstName(newFirstName)
            .lastName(newLastName)
            .build();
    }

    @Builder
    public record Create(
        UUID id,
        String firstName,
        String lastName
    ) {
        public Create(
            @NonNull UUID id,
            @NonNull String firstName,
            @NonNull String lastName
        ) {
            this.id = id;
            this.firstName = firstName.trim();
            this.lastName = lastName.trim();
        }

        public Author toAuthor() {
            return Author.builder()
                .id(this.id)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .build();
        }

        public void validate() {
            validateFirstName(firstName);
            validateLastName(lastName);
        }
    }

    public static final class Update {
        private final Map<Update.Field, Update.Item> updates;

        public Update(Iterable<Item> updates) {
            this.updates = new HashMap<>();
            for (final Item update : updates) {
                this.updates.put(update.field(), update);
            }
        }
        public Update(@NonNull Item... updates) {
            this(() -> Arrays.stream(updates).iterator());
        }

        public boolean isEmpty() {
            return updates.isEmpty();
        }

        public Collection<Update.Item> updates() {
            return updates.values();
        }

        public void validate() {
            this.updates().forEach(Item::validate);
        }

        public enum Field {
            FIRST_NAME,
            LAST_NAME
        }

        public sealed interface Item permits Update.Item.FirstName, Update.Item.LastName {
            Field field();

            void validate();

            boolean hasDifference(Author author);

            record FirstName(String value) implements Item {
                public FirstName(@NonNull String value) {
                    this.value = value.trim();
                }

                @Override
                public Field field() {
                    return Field.FIRST_NAME;
                }

                @Override
                public void validate() {
                    validateFirstName(this.value);
                }

                @Override
                public boolean hasDifference(Author author) {
                    return !Objects.equals(this.value, author.firstName());
                }
            }

            record LastName(String value) implements Item {
                public LastName(@NonNull String value) {
                    this.value = value.trim();
                }

                @Override
                public Field field() {
                    return Field.LAST_NAME;
                }

                @Override
                public void validate() {
                    validateLastName(this.value);
                }

                @Override
                public boolean hasDifference(Author author) {
                    return !Objects.equals(this.value, author.lastName());
                }
            }
        }
    }

    private static void validateFirstName(String value) {
        if (value.isBlank() || value.length() > 255) {
            throw BaseAppException.invalidFieldLength("firstName", 1, 255);
        }
    }

    private static void validateLastName(String value) {
        if (value.isBlank() || value.length() > 255) {
            throw BaseAppException.invalidFieldLength("lastName", 1, 255);
        }
    }
}
