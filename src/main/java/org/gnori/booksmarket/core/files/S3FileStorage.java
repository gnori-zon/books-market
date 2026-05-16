package org.gnori.booksmarket.core.files;

import java.net.URI;

import org.springframework.web.multipart.MultipartFile;

public interface S3FileStorage {
    boolean exists(String bucket, String id);

    void put(String bucket, String id, MultipartFile file);

    void delete(String bucket, String id);

    void copy(String fromBucket, String toBucket, String id);

    void ensureBucket(String bucket);

    void deleteByPrefix(String bucket, String prefix);

    URI buildUrlFor(String bucket, String id);
}
