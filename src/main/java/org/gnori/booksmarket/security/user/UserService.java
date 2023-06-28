package org.gnori.booksmarket.security.user;

public interface UserService {

  User update(String username, UserDto userDto);

  void delete(String username);
}
