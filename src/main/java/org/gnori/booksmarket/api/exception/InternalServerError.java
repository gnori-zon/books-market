package org.gnori.booksmarket.api.exception;

public class InternalServerError extends RuntimeException {

  public InternalServerError(String message) {
    super(message);
  }
}
