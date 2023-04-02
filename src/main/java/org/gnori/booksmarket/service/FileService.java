package org.gnori.booksmarket.service;

import org.springframework.core.io.FileSystemResource;

public interface FileService {
  FileSystemResource getFilesystemResource(byte[] bytes);
}
