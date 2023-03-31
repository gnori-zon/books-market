package org.gnori.booksmarket.api.service;

import java.io.File;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService{

  @Override
  public FileSystemResource getFilesystemResource(byte[] bytes) {
    try {
      // TODO: generate name file
      File temp = File.createTempFile("temp_file",".bin");
      temp.deleteOnExit();
      FileUtils.writeByteArrayToFile(temp,bytes);
      return new FileSystemResource(temp);
    } catch (IOException e) {
      return null;
    }
  }
}
