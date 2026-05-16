-- liquibase formatted sql
-- changeset gnori:001_init_db

CREATE TABLE authors
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL
);

CREATE TABLE genres
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX unique_genre_lower_name_idx ON genres (lower(name));

CREATE TABLE publishers
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX unique_publisher_lower_name_idx ON publishers (lower(name));

CREATE TABLE books
(
    id                    UUID PRIMARY KEY,
    name                  VARCHAR(255)   NOT NULL,
    description           VARCHAR(32767) NOT NULL,
    language              VARCHAR(30)    NOT NULL,
    release_date          DATE           NOT NULL,
    publisher_id          UUID           NOT NULL
        CONSTRAINT books_publisher_id_fkey REFERENCES publishers (id),
    cover_attachment_id   VARCHAR(100),
    content_attachment_id VARCHAR(100)   NOT NULL
);

CREATE TABLE book_authors
(
    book_id   UUID NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    author_id UUID NOT NULL
        CONSTRAINT book_authors_author_id_fkey REFERENCES authors (id),
    CONSTRAINT unique_book_author_pair UNIQUE (book_id, author_id)
);

CREATE INDEX idx_book_authors_book_id
    ON book_authors (book_id);

CREATE TABLE book_genres
(
    book_id  UUID NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    genre_id UUID NOT NULL
        CONSTRAINT book_genres_genre_id_fkey REFERENCES genres (id),
    CONSTRAINT unique_book_genre_pair UNIQUE (book_id, genre_id)
);

CREATE INDEX idx_book_genres_book_id
    ON book_genres (book_id);

