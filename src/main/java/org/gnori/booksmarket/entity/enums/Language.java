package org.gnori.booksmarket.entity.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

// ISO 639-3
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Language {
  ZHO("Chinese"),
  ENG("English"),
  SPA("Spanish"),
  ARA("Arab"),
  FRA("French"),
  RUS("Russian"),
  POR("Portuguese"),
  DEU("GERMAN");

  String language;
}
