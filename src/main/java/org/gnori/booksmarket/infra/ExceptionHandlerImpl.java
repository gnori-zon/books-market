package org.gnori.booksmarket.infra;

import java.util.ArrayList;
import java.util.List;

import org.gnori.booksmarket.core.exception.AppException;
import org.gnori.booksmarket.core.exception.AppExceptionList;
import org.gnori.booksmarket.core.exception.BaseAppException;
import org.gnori.booksmarket.core.exception.ErrorsDto;
import org.gnori.booksmarket.core.exception.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerImpl extends ResponseEntityExceptionHandler {

  @ResponseBody
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> hanleAuthenticationException(Exception ex) {

    final List<HttpException> exceptions = new ArrayList<>();
    if (ex instanceof AppExceptionList appExceptionList) {
      exceptions.addAll(appExceptionList.map(AppException::toHttpException));
    } else if (ex instanceof AppException appException) {
      exceptions.add(appException.toHttpException());
    } else if (ex instanceof HttpException httpEx) {
      exceptions.add(httpEx);
    } else if (ex instanceof NoHandlerFoundException noHandlerFoundException) {
      exceptions.add(new HttpException(() -> noHandlerFoundException.getMessage(), HttpStatus.NOT_FOUND));
    } else {
      exceptions.add(BaseAppException.unexpected(ex, log).toHttpException());
    }

    return ResponseEntity
        .status(exceptions.getFirst().status())
        .body(new ErrorsDto(
            exceptions.stream()
                .map(httpException -> new ErrorsDto.Item(httpException.status().value(), httpException.getMessage()))
                .toList()));
  }
}
