package org.gnori.booksmarket.feauture.book.core;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gnori.booksmarket.core.EntityType;
import org.gnori.booksmarket.core.exception.AppException;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.feauture.attachments.AttachmentService;
import org.gnori.booksmarket.feauture.author.Author;
import org.gnori.booksmarket.feauture.author.AuthorService;
import org.gnori.booksmarket.feauture.genre.Genre;
import org.gnori.booksmarket.feauture.genre.GenreService;
import org.gnori.booksmarket.feauture.publisher.Publisher;
import org.gnori.booksmarket.feauture.publisher.PublisherService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;
    private final GenreService genreService;
    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final AttachmentService attachmentService;

    public Book getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFoundBookById(id));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Book create(Book.Create create) {
        create.validate();
        final Publisher publisher = publisherService.getById(create.publisherId());
        final Map<UUID, Author> authorsById = authorService.listByIds(create.authorIds()).stream()
                .collect(Collectors.toMap(Author::id, Function.identity()));
        final Map<UUID, Genre> genresById = genreService.listByIds(create.genreIds()).stream()
                .collect(Collectors.toMap(Genre::id, Function.identity()));
        repository.create(create);
        createAttachments(create.id(), create.coverAttachmentId(), create.contentAttachmentId());
        return create.toBook(publisher, authorsById, genresById);
    }

    private void createAttachments(UUID bookId, String coverAttachmentId, @NonNull String contentAttachmentId) {
        attachmentService.copyToPermanent(contentAttachmentId);
        if (coverAttachmentId == null) {
            return;
        }
        try {
            attachmentService.copyToPermanent(coverAttachmentId);
        } catch (AppException e) {
            deleteContentAttachmentIdOrLogErr(bookId, contentAttachmentId);
            throw e;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Book updateById(UUID id, Book.Update update) {
        final Book exist = repository.findByIdForUpdate(id).orElseThrow(() -> notFoundBookById(id));
        final Book.Update actualUpdate = update.actualize(exist);
        if (!exist.hasDifferences(actualUpdate)) {
            return exist;
        }
        actualUpdate.validate();
        final Book.PublisherInfo actualPublisher = validateActualPublisher(actualUpdate, exist.publisher());
        final Map<UUID, Book.AuthorInfo> actualAuthors = validateActualAuthors(actualUpdate, exist.authors());
        final Map<UUID, Book.GenreInfo> actualGenres = validateActualGenres(actualUpdate, exist.genres());
        repository.updateById(id, actualUpdate);
        updateAttachments(
                exist,
                actualUpdate.get(Book.Update.Item.CoverAttachmentId.class),
                actualUpdate.get(Book.Update.Item.ContentAttachmentId.class));
        return exist.apply(actualUpdate, actualPublisher, actualAuthors, actualGenres);
    }

    private void updateAttachments(
            Book book,
            Book.Update.Item.CoverAttachmentId coverAttachmentId,
            Book.Update.Item.ContentAttachmentId contentAttachmentId) {
        if (coverAttachmentId == null && contentAttachmentId == null) {
            return;
        }
        Runnable rollbackAction = () -> {
        };
        if (contentAttachmentId != null) {
            attachmentService.copyToPermanent(contentAttachmentId.value());
            rollbackAction = () -> attachmentService.deleteFromPermanent(contentAttachmentId.value());
        }
        if (coverAttachmentId != null && coverAttachmentId.value() != null) {
            try {
                attachmentService.copyToPermanent(coverAttachmentId.value());
            } catch (AppException e) {
                rollbackAction.run();
                throw e;
            }
        }
        if (contentAttachmentId != null) {
            deleteContentAttachmentIdOrLogErr(book.id(), book.contentAttachmentId());
        }
        if (coverAttachmentId != null) {
            deleteCoverAttachmentIdOrLogErr(book.id(), book.coverAttachmentId());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteById(UUID id) {
        final var book = repository.findById(id).orElseThrow(() -> notFoundBookById(id));
        repository.deleteById(id);
        deleteAttachments(book.id(), book.coverAttachmentId(), book.contentAttachmentId());
    }

    private void deleteAttachments(
            @NonNull UUID bookId,
            String coverAttachmentId,
            @NonNull String contentAttachmentId) {
        deleteContentAttachmentIdOrLogErr(bookId, contentAttachmentId);
        deleteCoverAttachmentIdOrLogErr(bookId, coverAttachmentId);
    }

    private void deleteCoverAttachmentIdOrLogErr(@NotNull UUID bookId, String coverAttachmentId) {
        if (coverAttachmentId == null) {
            return;
        }
        try {
            attachmentService.deleteFromPermanent(coverAttachmentId);
        } catch (Exception e) {
            log.error("bad try delete cover attachment, bookId: {}, attachmentId: {}", bookId, coverAttachmentId, e);
        }
    }

    private void deleteContentAttachmentIdOrLogErr(@NotNull UUID bookId, @NotNull String contentAttachmentId) {
        try {
            attachmentService.deleteFromPermanent(contentAttachmentId);
        } catch (Exception e) {
            log.error("bad try delete content attachment, bookId: {}, attachmentId: {}", bookId, contentAttachmentId,
                    e);
        }
    }

    private Map<UUID, Book.GenreInfo> validateActualGenres(Book.Update update, Collection<Book.GenreInfo> existGenres) {
        final Map<UUID, Book.GenreInfo> actualGenres = existGenres.stream()
                .collect(Collectors.toMap(Book.GenreInfo::id, Function.identity()));
        if (!update.contains(Book.Update.Item.Genres.class)) {
            return Map.copyOf(actualGenres);
        }
        final var updateItem = update.get(Book.Update.Item.Genres.class);
        updateItem.deleteGenreIds()
                .forEach(actualGenres::remove);
        genreService.listByIds(updateItem.addGenreIds())
                .forEach(genre -> actualGenres.put(genre.id(), Book.GenreInfo.from(genre)));
        Book.validateGenresSize(actualGenres.size());
        return Map.copyOf(actualGenres);
    }

    private Map<UUID, Book.AuthorInfo> validateActualAuthors(Book.Update update,
            Collection<Book.AuthorInfo> existAuthors) {
        final Map<UUID, Book.AuthorInfo> actualAuthors = existAuthors.stream()
                .collect(Collectors.toMap(Book.AuthorInfo::id, Function.identity()));
        if (!update.contains(Book.Update.Item.Authors.class)) {
            return Map.copyOf(actualAuthors);
        }
        final var updateItem = update.get(Book.Update.Item.Authors.class);
        updateItem.deleteAuthorIds()
                .forEach(actualAuthors::remove);
        authorService.listByIds(updateItem.addAuthorIds())
                .forEach(author -> actualAuthors.put(author.id(), Book.AuthorInfo.from(author)));
        Book.validateAuthorsSize(actualAuthors.size());
        return Map.copyOf(actualAuthors);
    }

    private Book.PublisherInfo validateActualPublisher(Book.Update update, Book.PublisherInfo existPublisher) {
        if (!update.contains(Book.Update.Item.Publisher.class)) {
            return existPublisher;
        }
        final var updateItem = update.get(Book.Update.Item.Publisher.class);
        return Book.PublisherInfo.from(publisherService.getById(updateItem.publisherId()));
    }

    private AppException notFoundBookById(UUID id) {
        return BaseAppException.notFoundById(EntityType.BOOK, id);
    }
}
