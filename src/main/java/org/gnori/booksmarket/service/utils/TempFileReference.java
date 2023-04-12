package org.gnori.booksmarket.service.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TempFileReference extends PhantomReference<Object> {

  private final String path;

  public TempFileReference(File file, ReferenceQueue<? super Object> queue) {
    super(file, queue);
    this.path = file.getPath();
  }

  boolean delete() {
    File file = new File(path);
    if (!file.exists()) {
      return true;
    }

    try {
      Files.delete(Path.of(file.getPath()));
    } catch (IOException e) {
      log.error("[IOException] TempFileReference:" + e.getMessage());
      throw new RuntimeException(e);
    }
    return false;
  }
}
