package org.gnori.booksmarket.core.files.impl.minio;

import java.net.URI;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.core.files.S3FileStorage;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MinioS3FileStorage implements S3FileStorage {

    public record Config(
            String url,
            String accessKey,
            String secretKey) {
    }

    private final String s3Url;
    private final MinioClient minioClient;

    public MinioS3FileStorage(Config config) {
        this.s3Url = config.url();
        this.minioClient = MinioClient.builder()
                .endpoint(config.url())
                .credentials(config.accessKey(), config.secretKey())
                .build();
    }

    @Override
    public boolean exists(@NonNull String bucket, @NonNull String id) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build());
            return true;
        } catch (Exception e) {
            if (isNoSuchKey(e)) {
                return false;
            }
            throw BaseAppException.unexpected(e, log);
        }
    }

    @Override
    public void put(@NonNull String bucket, @NonNull String id, @NonNull MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (Exception e) {
            throw BaseAppException.unexpected(e, log);
        }
    }

    @Override
    public void copy(@NonNull String fromBucket, @NonNull String toBucket, @NonNull String id) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(toBucket)
                            .object(id)
                            .source(
                                    CopySource.builder()
                                            .bucket(fromBucket)
                                            .object(id)
                                            .build())
                            .build());
        } catch (Exception e) {
            if (e instanceof ErrorResponseException minioException && isNoSuchKey(minioException)) {
                throw BaseAppException.notFoundFile(id);
            }
            throw BaseAppException.unexpected(e, log);
        }
    }

    @Override
    public void delete(@NonNull String bucket, @NonNull String id) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build());
        } catch (Exception e) {
            throw BaseAppException.unexpected(e, log);
        }
    }

    @Override
    public void deleteByPrefix(@NonNull String bucket, @NonNull String prefix) {
        final Iterable<Result<Item>> foundedObjects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .build());
        final Iterable<DeleteObject> toDelete = () -> StreamSupport.stream(foundedObjects.spliterator(), false)
                .map(object -> {
                    try {
                        return Optional.of(new DeleteObject(object.get().objectName()));
                    } catch (Exception e) {
                        log.warn("error while getting from objectResult, object will be skipped for deleteByPrefix", e);
                        return Optional.<DeleteObject>empty();
                    }
                })
                .flatMap(Optional::stream)
                .iterator();
        final Iterable<Result<DeleteError>> deleteErrors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .objects(toDelete)
                        .build());
        deleteErrors.iterator().forEachRemaining(error -> {
            try {
                final var errorInfo = error.get();
                log.warn("error while deleteByPrefix bucket: {}, object: {}, code: {}, message: {}",
                        errorInfo.bucketName(),
                        errorInfo.objectName(),
                        errorInfo.code(),
                        errorInfo.message());
            } catch (Exception e) {
                log.warn("error while getting from errorResult", e);
            }
        });
    }

    @Override
    public void ensureBucket(@NonNull String bucket) {
        try {
            if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                return;
            }
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            throw BaseAppException.unexpected(e, log);
        }
    }

    @Override
    public URI buildUrlFor(String bucket, String id) {
        return URI.create(String.format("%s/%s/%s", s3Url, bucket, id));
    }

    private static final String NO_SUCH_KEY = "NoSuchKey";

    private static boolean isNoSuchKey(Exception exception) {
        if (exception instanceof ErrorResponseException minioException) {
            return NO_SUCH_KEY.equals(minioException.errorResponse().code());
        }
        return false;
    }
}
