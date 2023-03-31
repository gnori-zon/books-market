package org.gnori.booksmarket.api.service;

import org.springframework.core.io.FileSystemResource;

public interface FileService {
  FileSystemResource getFilesystemResource(byte[] bytes);
}
