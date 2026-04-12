package com.czetsuyatech.envers.service.impl;

import com.czetsuyatech.envers.application.dto.UserDTO;
import com.czetsuyatech.envers.application.mapper.AddressMapper;
import com.czetsuyatech.envers.application.mapper.UserMapper;
import com.czetsuyatech.envers.persistence.repository.AddressRepository;
import com.czetsuyatech.envers.persistence.repository.UserRepository;
import com.czetsuyatech.envers.service.UserService;
import com.github.javafaker.Faker;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AddressRepository addressRepository;
  private final UserMapper userMapper;
  private final AddressMapper addressMapper;

  @Override
  @Transactional
  public UserDTO create(UserDTO userDTO) {

    var user = Optional.ofNullable(userDTO)
        .map(userMapper::toEntity)
        .map(userRepository::save)
        .orElseThrow(() -> new IllegalArgumentException("UserDTO cannot be null"));

    return userRepository.findById(user.getId())
        .map(userMapper::toDto)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @Transactional
  @Override
  public void update(Long userId, UserDTO userDTO) {

    Faker faker = new Faker();

    Optional.ofNullable(userId)
        .map(addressRepository::findByUserId)
        .filter(addresses -> !addresses.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("User not found"))
        .forEach(address -> {
          address.setStreet(faker.address().streetName());
          addressRepository.save(address);
        });

    Optional.ofNullable(userId)
        .flatMap(userRepository::findById)
        .map(user -> userMapper.toEntity(userDTO, user))
        .map(userRepository::save);
  }

  @Transactional
  @Override
  public void delete(Long userId) {
    userRepository.deleteById(userId);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<UserDTO> getById(Long userId) {
    return userRepository.findById(userId)
        .map(userMapper::toDto);
  }
}
