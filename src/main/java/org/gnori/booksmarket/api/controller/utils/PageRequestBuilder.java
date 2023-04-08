package org.gnori.booksmarket.api.controller.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestBuilder {

  private static final String ASC = "ASC";
  private static final String DESC = "DESC";
  private static final String FIELD_FIRST_NAME = "firstName";
  private static final String FIElD_LAST_NAME = "lastName";
  private static final String FIElD_NAME = "name";
  private static final String FIElD_RELEASE_DATE = "releaseDate";

  public static PageRequest buildPageRequestForFirstAndLastName(Integer page, Integer size,
      Optional<String> sortByFirstName, Optional<String> sortByLastName){

    ArrayList<Sort> sortList = getSortParamsList(Map.of(FIELD_FIRST_NAME,
        sortByFirstName, FIElD_LAST_NAME, sortByLastName));

    if (sortList.size() > 1) {
      return PageRequest.of(page, size, sortList.get(0).and(sortList.get(1)));
    }

    if (sortList.size() == 1) {
      return PageRequest.of(page, size, sortList.get(0));
    }

    return PageRequest.of(page,size);
  }

  public static PageRequest buildPageRequestForName(Integer page, Integer size,
      Optional<String> sortByName) {

    var sortList = getSortParamsList(Map.of(FIElD_NAME, sortByName));

    if (sortList.size() == 1) {
      return PageRequest.of(page, size, sortList.get(0));
    }

    return PageRequest.of(page,size);

  }

  public static PageRequest buildPageRequestForNameAndReleaseDate(Integer page, Integer size,
      Optional<String> sortByName, Optional<String> sortByReleaseDate) {

    var sortList = getSortParamsList(Map.of(FIElD_NAME,
        sortByName, FIElD_RELEASE_DATE, sortByReleaseDate));

    if (sortList.size() > 1) {
      return PageRequest.of(page, size, sortList.get(0).and(sortList.get(1)));
    }

    if (sortList.size() == 1) {
      return PageRequest.of(page, size, sortList.get(0));
    }

    return PageRequest.of(page,size);
  }

  private static ArrayList<Sort> getSortParamsList(Map<String,Optional<String>> sortByParamAndFieldMap) {

    var sortList = new ArrayList<Sort>();

    for (var field : sortByParamAndFieldMap.keySet()) {

      var param = sortByParamAndFieldMap.get(field);

      if (param.isPresent()) {

        if (param.get().equalsIgnoreCase(ASC)) {
          sortList.add(Sort.by(field).ascending());

        } else if (param.get().equalsIgnoreCase(DESC)) {
          sortList.add(Sort.by(field).descending());

        }
      }

    }
    return sortList;
  }
}
