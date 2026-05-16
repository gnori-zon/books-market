package org.gnori.booksmarket.feauture.attachments;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.gnori.booksmarket.core.files.S3FileStorage;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttachmentService {

    public record Config(
            String temporaryBucket,
            String permanentBucket,
            Attachment.Constraints constraints) {
    }

    private final S3FileStorage fileStorage;
    private final Config config;

    @PostConstruct
    public void initBuckets() {
        fileStorage.ensureBucket(config.temporaryBucket());
        fileStorage.ensureBucket(config.permanentBucket());
    }

    public String saveToTemporary(@NonNull Attachment attachment) {
        attachment.validate(config.constraints());
        final String id = generateId();
        fileStorage.put(config.temporaryBucket(), id, attachment.file());
        return mask(id);
    }

    public void copyToPermanent(@NonNull String id) {
        final String unmaskedId = unmask(id);
        fileStorage.copy(config.temporaryBucket(), config.permanentBucket(), unmaskedId);
    }

    public void deleteFromPermanent(@NonNull String id) {
        final String unmaskedId = unmask(id);
        fileStorage.delete(config.permanentBucket(), unmaskedId);
    }

    public void cleanTemporaryByHour(UnaryOperator<LocalDateTime> dateSupplier) {
        final LocalDateTime date = dateSupplier.apply(LocalDateTime.now(ZoneOffset.UTC));
        final String prefix = generatePrefixId(date);
        fileStorage.deleteByPrefix(config.temporaryBucket(), prefix);
    }

    public URI getUrlForPermanent(String id) {
        final String unmaskedId = unmask(id);
        return fileStorage.buildUrlFor(config.permanentBucket(), unmaskedId);
    }

    private final Charset CHARSET = StandardCharsets.UTF_8;

    private String mask(String value) {
        final byte[] bytes = Base64.getEncoder().encode(value.getBytes(CHARSET));
        return new String(bytes, CHARSET);
    }

    private String unmask(String value) {
        final byte[] bytes = Base64.getDecoder().decode(value.getBytes(CHARSET));
        return new String(bytes, CHARSET);
    }

    private String generatePrefixId(LocalDateTime time) {
        return String.format("%d/%d/%d/%d/",
                time.getYear(),
                time.getMonth().getValue(),
                time.getDayOfMonth(),
                time.getHour());
    }

    private String generateId() {
        final String prefix = generatePrefixId(LocalDateTime.now(ZoneOffset.UTC));
        return String.format("%s%s", prefix, UUID.randomUUID());
    }
}
