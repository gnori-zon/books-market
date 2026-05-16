package org.gnori.booksmarket.feauture.attachments;

import org.gnori.booksmarket.core.files.S3FileStorage;
import org.gnori.booksmarket.core.files.impl.minio.MinioS3FileStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttachmentConfiguration {

    @Bean
    S3FileStorage s3FileStorage(AttachmentProperties properties) {
        final AttachmentProperties.Connection connection = properties.connection();
        return switch (connection.type()) {
            case MINIO -> new MinioS3FileStorage(
                    new MinioS3FileStorage.Config(
                            connection.url(),
                            connection.accessKey(),
                            connection.secretKey()));
        };
    }

    @Bean
    AttachmentService attachmentService(
            AttachmentProperties properties,
            S3FileStorage s3FileStorage) {
        return new AttachmentService(
                s3FileStorage,
                new AttachmentService.Config(
                        properties.temporaryBucket(),
                        properties.permanentBucket(),
                        new Attachment.Constraints(5_242_880, 100 * 5_242_880)));
    }
}
