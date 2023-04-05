package org.gnori.booksmarket.service.utils;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class TempFileReference extends PhantomReference<Object> {

  private final String path;

  public TempFileReference(File file, ReferenceQueue<? super Object> queue) {
    super(file, queue);
    this.path = file.getPath();
  }

  boolean delete() {
    File file = new File(path);
    if (file == null || !file.exists()) {
      return true;
    }
    return file.delete();
  }
}
