package org.gnori.booksmarket.api.controller.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageRequestBuilder {

  private PageRequestBuilder(){}

  private static final String ASC = "ASC";
  private static final String DESC = "DESC";
  private static final String FIRST_NAME_FIELD = "firstName";
  private static final String LAST_NAME_FIElD = "lastName";
  private static final String NAME_FIElD = "name";
  private static final String RELEASE_DATE_FIElD = "releaseDate";

  public static PageRequest buildPageRequestForFirstAndLastName(Integer page, Integer size,
      Optional<String> sortByFirstName, Optional<String> sortByLastName) {

    ArrayList<Sort> sortList = getSortParamsList(Map.of(FIRST_NAME_FIELD,
        sortByFirstName, LAST_NAME_FIElD, sortByLastName));

    return getPageRequest(sortList, page, size);
  }

  public static PageRequest buildPageRequestForName(Integer page, Integer size,
      Optional<String> sortByName) {

    var sortList = getSortParamsList(Map.of(NAME_FIElD, sortByName));

    if (sortList.size() == 1) {
      return PageRequest.of(page, size, sortList.get(0));
    }

    return PageRequest.of(page, size);

  }

  public static PageRequest buildPageRequestForNameAndReleaseDate(Integer page, Integer size,
      Optional<String> sortByName, Optional<String> sortByReleaseDate) {

    var sortList = getSortParamsList(Map.of(NAME_FIElD,
        sortByName, RELEASE_DATE_FIElD, sortByReleaseDate));

    return getPageRequest(sortList, page, size);
  }

  private static ArrayList<Sort> getSortParamsList(
      Map<String, Optional<String>> sortByParamAndFieldMap) {

    var sortList = new ArrayList<Sort>();

    for (Map.Entry<String, Optional<String>> entry : sortByParamAndFieldMap.entrySet()) {
      var field = entry.getKey();
      var param = entry.getValue();

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

  private static PageRequest getPageRequest(List<Sort> params, Integer page, Integer size) {

    if (params.size() > 1) {
      return PageRequest.of(page, size, params.get(0).and(params.get(1)));
    }

    if (params.size() == 1) {
      return PageRequest.of(page, size, params.get(0));
    }

    return PageRequest.of(page, size);
  }
}