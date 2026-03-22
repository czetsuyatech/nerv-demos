package com.czetsuyatech.envers.controllers;

import com.czetsuyatech.envers.persistence.entity.UserEntity;
import com.czetsuyatech.envers.persistence.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserRepository userRepository;
  private final EntityManager entityManager;

  @PostMapping
  public UserEntity saveUser() {
    return userRepository.save(getUser());
  }

  @PutMapping("/{userId}")
  public void updateUser(@PathVariable Long userId) {
    userRepository.findById(userId)
        .map(user -> {
          user.setFirstName("czetsuya");
          user.setLastName("tech");
          return user;
        })
        .map(user -> userRepository.save(user));
  }

  @DeleteMapping("/{userId}")
  public void deleteUser(@PathVariable Long userId) {
    userRepository.deleteById(userId);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserEntity> findByUser(@PathVariable Long userId) {
    return ResponseEntity.of(userRepository.findById(userId));
  }

  @GetMapping("/{userId}/revisions")
  public Object getRevisions() {

    AuditReader auditReader = AuditReaderFactory.get(entityManager);
    var revisions = auditReader.getRevisions(UserEntity.class, 1);
    return auditReader.find(UserEntity.class, 1, revisions.getLast());
  }

  private static UserEntity getUser() {

    return UserEntity.builder()
        .firstName("Edward")
        .lastName("Legaspi")
        .birthDate(LocalDateTime.now())
        .username("czetsuya")
        .hobbies(List.of("chess"))
        .build();
  }
}
