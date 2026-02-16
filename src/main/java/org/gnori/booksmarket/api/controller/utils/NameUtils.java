package org.gnori.booksmarket.api.controller.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NameUtils {

  public static String processName(String name) {
      return StringUtils.capitalize(name.trim());
  }
}
