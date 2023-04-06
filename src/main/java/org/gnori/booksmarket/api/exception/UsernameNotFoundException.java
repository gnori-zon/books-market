package org.gnori.booksmarket.api.exception;

import org.springframework.http.HttpStatus;

public class UsernameNotFoundException extends BusinessException{

  public UsernameNotFoundException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}
