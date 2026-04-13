package com.czetsuyatech.envers.web.controller;

import com.czetsuyatech.envers.application.dto.AddressDTO;
import com.czetsuyatech.envers.application.dto.UserDTO;
import com.czetsuyatech.envers.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public UserDTO createUser(@RequestBody UserDTO userDTO) {
    return userService.create(userDTO);
  }

  @PutMapping("/{userId}")
  public void updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) {

    userService.update(userId, userDTO);
  }

  @PutMapping("/{userId}/addresses/{addressId}")
  public void updateAddress(@PathVariable Long userId, @PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {

    userService.updateAddress(addressId, addressDTO);
  }

  @DeleteMapping("/{userId}")
  public void deleteUser(@PathVariable Long userId) {
    userService.delete(userId);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
    return ResponseEntity.of(userService.getById(userId));
  }
}
