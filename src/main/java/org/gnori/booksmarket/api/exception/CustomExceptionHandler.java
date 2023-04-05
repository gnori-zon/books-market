package org.gnori.booksmarket.api.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  @ResponseBody
  public ResponseEntity<Object> handleAuthenticationException(RuntimeException ex){
    log.error("Exception during execution of application", ex);

    HttpStatus status;
    if (ex instanceof BusinessException businessException) {
      status = businessException.getStatus();
    } else {
      status = HttpStatus.FORBIDDEN;
    }

    return ResponseEntity
        .status(status)
        .body(ErrorDto.builder()
            .error(status.getReasonPhrase())
            .errorDescription(ex.getMessage())
            .build());
  }
}
