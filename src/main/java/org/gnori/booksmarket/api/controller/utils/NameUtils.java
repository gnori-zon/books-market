package org.gnori.booksmarket.api.controller.utils;

public class NameUtils {

  private NameUtils(){}

  public static String processName(String name) {
    name = name.trim();
    return name.length() >= 2 ?
        String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1).toLowerCase() :
        name.toUpperCase();
  }
}
