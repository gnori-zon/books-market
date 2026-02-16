-- liquibase formatted sql
-- changeset gnori:001_init_db

CREATE TABLE users
(
    id       UUID PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE authors
(
    id         UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL
);

CREATE TABLE genres
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE publishers
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE books
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    language     VARCHAR(30)  NOT NULL,
    release_date DATE         NOT NULL,
    publisher_id UUID         NOT NULL REFERENCES publishers (id)
);

CREATE TABLE binary_contents
(
    id         UUID PRIMARY KEY,
    image      BYTEA,
    raw        BYTEA        NOT NULL,
    size_raw   DOUBLE PRECISION,
    type_image VARCHAR(255),
    type_raw   VARCHAR(255) NOT NULL,
    book_id    UUID         NOT NULL REFERENCES books (id)
);

CREATE TABLE book_authors
(
    book_id   UUID NOT NULL REFERENCES books (id),
    author_id UUID NOT NULL REFERENCES authors (id)
);

CREATE TABLE book_genres
(
    book_id  UUID NOT NULL REFERENCES books (id),
    genre_id UUID NOT NULL REFERENCES genres (id)
);

CREATE TABLE publisher_authors
(
    publisher_entity_id UUID NOT NULL REFERENCES publishers (id),
    authors_id          UUID NOT NULL REFERENCES authors (id)
);

CREATE TABLE reviews
(
    id      UUID PRIMARY KEY,
    book_id UUID         NOT NULL REFERENCES books (id),
    content VARCHAR(255) NOT NULL,
    title   VARCHAR(255) NOT NULL
);