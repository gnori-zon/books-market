package org.gnori.booksmarket.security.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.security.config.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

  private static final String USER_URL = "/api/user";

  UserService userService;
  JwtService jwtService;

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = USER_URL,
      produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public UserDto changeUserData(
      @RequestBody UserDto userDto,
      @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

    var username = jwtService.extractUsername(token.substring(7));

    var user = userService.update(username, userDto);

    var newToken = jwtService.generateToken(user);

    var responseDto = new UserDto();
    responseDto.setUsername(user.getUsername());
    responseDto.setRole(user.getRole());
    responseDto.setToken(newToken);

    return responseDto;
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(USER_URL)
  public void delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

    var username = jwtService.extractUsername(token.substring(7));

    userService.delete(username);
  }

}
