package org.gnori.booksmarket.security.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.exception.UsernameNotFoundException;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.security.user.Role;
import org.gnori.booksmarket.security.user.User;
import org.gnori.booksmarket.security.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

  UserRepository userRepository;

  PasswordEncoder passwordEncoder;

  JwtService jwtService;

  AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {

    var user = User.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();

    userRepository.saveAndFlush(user);

    var jwt = jwtService.generateToken(user);

    return AuthenticationResponse.builder()
        .token(jwt)
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    var user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    var jwt = jwtService.generateToken(user);

    return AuthenticationResponse.builder()
        .token(jwt)
        .build();
  }
}
