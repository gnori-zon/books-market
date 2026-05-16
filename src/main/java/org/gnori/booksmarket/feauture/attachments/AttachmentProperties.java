package org.gnori.booksmarket.feauture.attachments;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "attachments")
public record AttachmentProperties(
    Connection connection,
    String temporaryBucket,
    String permanentBucket
) {
    public record Connection(
        Type type,
        String url,
        String accessKey,
        String secretKey
    ) {
        public enum Type {
            MINIO
        }
    }
}
