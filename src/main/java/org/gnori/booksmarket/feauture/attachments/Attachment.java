package org.gnori.booksmarket.feauture.attachments;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;

public sealed interface Attachment permits Attachment.Cover, Attachment.Content {
    MultipartFile file();

    void validate(Constraints constraints);

    record Constraints(
            long coverMaxSizeBytes,
            long contentMaxSizeBytes) {
        public Constraints {
            if (coverMaxSizeBytes < 1) {
                throw new IllegalArgumentException("coverMaxSize should be positive number");
            }
            if (contentMaxSizeBytes < 1) {
                throw new IllegalArgumentException("contentMaxSize should be positive number");
            }
        }
    }

    record Cover(
            MultipartFile file) implements Attachment {

        private static final Set<String> AVAILABLE_COVER_TYPES = Set.of(
                IMAGE_JPEG.toString(),
                IMAGE_PNG.toString());

        @Override
        public void validate(Constraints constraints) {
            if (!AVAILABLE_COVER_TYPES.contains(file.getContentType())) {
                throw AttachmentExceptions.invalidCoverFileType(AVAILABLE_COVER_TYPES, file.getContentType());
            }
            if (file.getSize() > constraints.coverMaxSizeBytes()) {
                throw AttachmentExceptions.exceedCoverMaxSize(constraints.coverMaxSizeBytes(), file.getSize());
            }
        }
    }

    record Content(
            MultipartFile file) implements Attachment {
        private static final Set<String> AVAILABLE_CONTENT_TYPES = Set.of(
                APPLICATION_PDF.toString());

        @Override
        public void validate(Constraints constraints) {
            if (!AVAILABLE_CONTENT_TYPES.contains(file.getContentType())) {
                throw AttachmentExceptions.invalidContentFileType(AVAILABLE_CONTENT_TYPES, file.getContentType());
            }
            if (file.getSize() > constraints.contentMaxSizeBytes()) {
                throw AttachmentExceptions.exceedContentMaxSize(constraints.contentMaxSizeBytes(), file.getSize());
            }
        }
    }
}
