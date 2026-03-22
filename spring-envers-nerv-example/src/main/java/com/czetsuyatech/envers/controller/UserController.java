package com.czetsuyatech.envers.controller;

import com.czetsuyatech.envers.persistence.entity.UserEntity;
import com.czetsuyatech.envers.service.UserService;
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

  private final UserService userService;

  @PostMapping
  public UserEntity createUser() {
    return userService.create();
  }

  @PutMapping("/{userId}")
  public void updateUser(@PathVariable Long userId) {
    userService.update(userId);
  }

  @DeleteMapping("/{userId}")
  public void deleteUser(@PathVariable Long userId) {
    userService.delete(userId);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserEntity> getUser(@PathVariable Long userId) {
    return ResponseEntity.of(userService.getById(userId));
  }

  @GetMapping("/{userId}/revisions")
  public Object getRevisions(@PathVariable Long userId) {
    return userService.getRevisions(userId);
  }
}
