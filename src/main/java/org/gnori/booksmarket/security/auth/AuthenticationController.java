package org.gnori.booksmarket.security.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.aop.LogExecutionTime;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

  private final static String AUTHENTICATION_URL = "/api/auth";

  AuthenticationService authenticationService;

  @LogExecutionTime
  @PostMapping(AUTHENTICATION_URL+"/register")
  public ResponseEntity<AuthenticationResponse> register (
      @RequestBody RegisterRequest request) {

    return ResponseEntity.ok(authenticationService.register(request));
  }

  @LogExecutionTime
  @PostMapping(AUTHENTICATION_URL+"/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate (
      @RequestBody AuthenticationRequest request) {

    return ResponseEntity.ok(authenticationService.authenticate(request));
  }
}
