package org.gnori.booksmarket.security.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

  UserRepository userRepository;

  PasswordEncoder passwordEncoder;

  @Override
  public User update(String username, UserDto userDto) {

    var user = userRepository.findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException(String.format("user with name: %s not found", username))
        );

    if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    if (userDto.getUsername() != null && !userDto.getUsername().isBlank() && !userDto.getUsername().equals(username)) {
      if (userRepository.existsByUsername(userDto.getUsername())) {
        throw new BadRequestException(String.format("user with name: %s already is exist", username));
      }
      user.setUsername(userDto.getUsername());
    }

    return userRepository.saveAndFlush(user);
  }

  @Override
  public void delete(String username) {

    if (!userRepository.existsByUsername(username)) {
      throw new UsernameNotFoundException(String.format("user with name: %s not found", username));
    }

    userRepository.deleteByUsername(username);
  }
}
