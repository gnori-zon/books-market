package org.gnori.booksmarket.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FileUtils;
import org.gnori.booksmarket.service.FileService;
import org.gnori.booksmarket.service.utils.TempFileCleanerThread;
import org.gnori.booksmarket.service.utils.TempFileReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {
  ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();

  public FileServiceImpl() {
    var tempFileCleanerThread = new TempFileCleanerThread(referenceQueue);
    tempFileCleanerThread.start();
  }

  @Override
  public FileSystemResource getFilesystemResource(byte[] bytes, String fileName) {
    try {
      File temp = File.createTempFile(fileName,".bin");

      new TempFileReference(temp, referenceQueue);
      temp.deleteOnExit();

      FileUtils.writeByteArrayToFile(temp,bytes);

      return new FileSystemResource(temp);
    } catch (IOException e) {

      return null;
    }
  }
}
