package org.gnori.booksmarket.feauture.publisher;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.gnori.booksmarket.core.exception.BaseAppException;

@Builder
public record Publisher(
    UUID id,
    String name
) {
    public boolean hasDifferences(Publisher.Update update) {
        for (final var item : update.updates()) {
            if (item.hasDifference(this)) {
                return true;
            }
        }
        return false;
    }

    public Publisher apply(Publisher.Update update) {
        var newName = this.name;
        for (final var item : update.updates()) {
            switch (item) {
                case Publisher.Update.Item.Name(String value):
                    newName = value;
                    break;
            }
        }
        return Publisher.builder()
            .id(this.id)
            .name(newName)
            .build();
    }

    @Builder
    public record Create(
        UUID id,
        String name
    ) {
        public Create(
            @NonNull UUID id,
            @NonNull String name
        ) {
            this.id = id;
            this.name = name.trim().toLowerCase();
        }

        public Publisher toPublisher() {
            return Publisher.builder()
                .id(id)
                .name(name)
                .build();
        }

        public void validate() {
            validateName(this.name);
        }
    }

    public static final class Update {
        private final Map<Publisher.Update.Field, Publisher.Update.Item> updates;
        public Update(Iterable<Publisher.Update.Item> updates) {
            this.updates = new HashMap<>();
            for (final Publisher.Update.Item update : updates) {
                this.updates.put(update.field(), update);
            }
        }
        public Update(@NonNull Publisher.Update.Item... updates) {
            this(() -> Arrays.stream(updates).iterator());
        }

        public boolean isEmpty() {
            return updates.isEmpty();
        }

        public Collection<Publisher.Update.Item> updates() {
            return updates.values();
        }

        public void validate() {
            this.updates().forEach(Publisher.Update.Item::validate);
        }

        public <I extends Update.Item> boolean contains(Class<I> itemClazz) {
            return updates.containsKey(Update.Field.from(itemClazz));
        }

        public <I extends Update.Item> I get(Class<I> itemClazz) {
            return itemClazz.cast(updates.get(Update.Field.from(itemClazz)));
        }

        @Getter(AccessLevel.PRIVATE)
        @Accessors(fluent = true)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public enum Field {
            NAME(Update.Item.Name.class);

            public static final Map<Class<? extends Update.Item>, Update.Field> FIELD_BY_ITEM_CLASS = Arrays.stream(values())
                .collect(Collectors.toMap(Update.Field::itemClazz, Function.identity()));

            public static <I extends Update.Item> Field from(Class<I> itemClazz) {
                return Objects.requireNonNull(FIELD_BY_ITEM_CLASS.get(itemClazz));
            }

            private final Class<? extends Update.Item> itemClazz;
        }

        public sealed interface Item permits Publisher.Update.Item.Name {
            Publisher.Update.Field field();

            void validate();

            boolean hasDifference(Publisher author);

            record Name(String value) implements Publisher.Update.Item {
                public Name(@NonNull String value) {
                    this.value = value.trim().toLowerCase();
                }

                @Override
                public Publisher.Update.Field field() {
                    return Publisher.Update.Field.NAME;
                }

                @Override
                public void validate() {
                    validateName(this.value);
                }

                @Override
                public boolean hasDifference(Publisher publisher) {
                    return !Objects.equals(this.value, publisher.name());
                }
            }
        }
    }

    private static void validateName(String value) {
        if (value.isBlank() || value.length() > 255) {
            throw BaseAppException.invalidFieldLength("name", 1, 255);
        }
    }
}

