package org.gnori.booksmarket.service.utils;

import java.lang.ref.ReferenceQueue;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TempFileCleanerThread extends Thread{
  private final ReferenceQueue<Object> referenceQueue;
  public TempFileCleanerThread(ReferenceQueue<Object> referenceQueue) {
    super("TempFileCleaner");
    this.referenceQueue = referenceQueue;
    setDaemon(true);
  }

  @Override
  public void run() {
    log.info("[STARTED] TempFileCleaner :" + this);
    while (true) {
      TempFileReference unusedReference;
      try {
        unusedReference = (TempFileReference) referenceQueue.remove();

        log.info("[CLEARED_RESOURCE] TempFileCleaner :" + this);
        unusedReference.delete();
        unusedReference.clear();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
