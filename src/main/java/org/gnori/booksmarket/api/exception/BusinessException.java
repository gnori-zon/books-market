package org.gnori.booksmarket.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class BusinessException extends AuthenticationException {
  private final HttpStatus status;
  public BusinessException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
