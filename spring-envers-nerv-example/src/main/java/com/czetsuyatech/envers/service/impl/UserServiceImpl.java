package com.czetsuyatech.envers.service.impl;

import com.czetsuyatech.envers.persistence.entity.UserEntity;
import com.czetsuyatech.envers.persistence.repository.UserRepository;
import com.czetsuyatech.envers.service.UserService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final EntityManager entityManager;

  @Override
  public UserEntity create() {
    return userRepository.save(getUser());
  }

  @Override
  public void update(Long userId) {

    userRepository.findById(userId)
        .map(user -> {
          user.setFirstName("czetsuya");
          user.setLastName("tech");
          user.getHobbies().add("Anime");
          return user;
        })
        .map(user -> userRepository.save(user));
  }

  @Override
  public void delete(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public Optional<UserEntity> getById(Long userId) {
    return userRepository.findById(userId);
  }

  private static UserEntity getUser() {

    return UserEntity.builder()
        .firstName("Edward")
        .lastName("Legaspi")
        .birthDate(LocalDateTime.now())
        .username("czetsuya")
        .hobbies(List.of("Chess"))
        .build();
  }
}
