package org.gnori.booksmarket.core.exception;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Builder;

@Builder
public class ErrorsDto extends ArrayList<ErrorsDto.Item> {

  public record Item(
      int code,
      String message) {
  }

  public ErrorsDto() {
    super();
  }

  public ErrorsDto(Collection<? extends ErrorsDto.Item> errors) {
    super(errors);
  }
}
