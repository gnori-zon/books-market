package org.gnori.booksmarket.service.utils;

import java.lang.ref.ReferenceQueue;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TempFileCleanerThread extends Thread{

  private static final String INFO_TEXT_TEMP_FILE_CLEANER_RUN = "[STARTED] TempFileCleaner : {}";
  private static final String INFO_TEXT_TEMP_FILE_CLEARED_RESOURCE = "[CLEARED_RESOURCE] TempFileCleaner : {}";
  private static final String ERROR_TEXT_INTERRUPTED_EXCEPTION = "[INTERRUPTED_EXCEPTION] TempFileCleaner : {}";

  private final ReferenceQueue<Object> referenceQueue;
  public TempFileCleanerThread(ReferenceQueue<Object> referenceQueue) {
    super("TempFileCleaner");
    this.referenceQueue = referenceQueue;
    setDaemon(true);
  }

  @Override
  public void run() {
    while (true) {
      log.info(INFO_TEXT_TEMP_FILE_CLEANER_RUN, this);
      TempFileReference unusedReference;
      try {
        unusedReference = (TempFileReference) referenceQueue.remove();

        log.info(INFO_TEXT_TEMP_FILE_CLEARED_RESOURCE, this);
        unusedReference.delete();
        unusedReference.clear();
      } catch (InterruptedException e) {

        log.error(ERROR_TEXT_INTERRUPTED_EXCEPTION, e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
  }

}
