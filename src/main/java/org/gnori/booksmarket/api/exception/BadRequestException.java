package org.gnori.booksmarket.api.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException{

  public BadRequestException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}
