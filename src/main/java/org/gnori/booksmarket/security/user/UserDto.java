package org.gnori.booksmarket.security.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

  String username;

  String password;

  String token;

  Role role;
}
