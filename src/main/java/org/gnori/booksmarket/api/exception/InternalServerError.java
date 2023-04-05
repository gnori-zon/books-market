package org.gnori.booksmarket.api.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends BusinessException {

  public InternalServerError(String message) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
