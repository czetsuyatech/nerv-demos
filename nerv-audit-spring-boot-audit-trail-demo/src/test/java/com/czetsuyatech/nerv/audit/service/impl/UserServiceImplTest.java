package com.czetsuyatech.nerv.audit.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.czetsuyatech.nerv.audit.application.dto.AddressDTO;
import com.czetsuyatech.nerv.audit.application.dto.UserDTO;
import com.czetsuyatech.nerv.audit.application.mapper.AddressMapper;
import com.czetsuyatech.nerv.audit.application.mapper.UserMapper;
import com.czetsuyatech.nerv.audit.persistence.entity.AddressEntity;
import com.czetsuyatech.nerv.audit.persistence.entity.UserEntity;
import com.czetsuyatech.nerv.audit.persistence.repository.AddressRepository;
import com.czetsuyatech.nerv.audit.persistence.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private AddressRepository addressRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private AddressMapper addressMapper;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  void create_savesMappedEntityAndReturnsMappedResult() {
    UserDTO request = UserDTO.builder().username("alice").build();
    UserEntity entity = UserEntity.builder().id(1L).username("alice").build();
    UserDTO result = UserDTO.builder().id(1L).username("alice").build();
    when(userMapper.toEntity(request)).thenReturn(entity);
    when(userRepository.save(entity)).thenReturn(entity);
    when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(userMapper.toDto(entity)).thenReturn(result);

    UserDTO created = userService.create(request);

    assertThat(created).isSameAs(result);
    verify(userRepository).save(entity);
  }

  @Test
  void create_rejectsNullRequest() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> userService.create(null))
        .withMessage("UserDTO cannot be null");
  }

  @Test
  void update_savesExistingUser() {
    UserDTO request = UserDTO.builder().username("updated").build();
    UserEntity existing = UserEntity.builder().id(3L).username("old").build();
    when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
    when(userMapper.toEntity(request, existing)).thenReturn(existing);

    userService.update(3L, request);

    verify(userRepository).save(existing);
  }

  @Test
  void getById_mapsExistingUser() {
    UserEntity entity = UserEntity.builder().id(4L).username("alice").build();
    UserDTO dto = UserDTO.builder().id(4L).username("alice").build();
    when(userRepository.findById(4L)).thenReturn(Optional.of(entity));
    when(userMapper.toDto(entity)).thenReturn(dto);

    assertThat(userService.getById(4L)).containsSame(dto);
  }

  @Test
  void delete_delegatesToRepository() {
    userService.delete(5L);

    verify(userRepository).deleteById(5L);
  }

  @Test
  void updateAddress_mapsAndSavesExistingAddress() {
    AddressDTO request = AddressDTO.builder().city("Manila").build();
    AddressEntity existing = AddressEntity.builder().id(6L).city("Old City").build();
    when(addressRepository.findById(6L)).thenReturn(Optional.of(existing));
    when(addressRepository.save(existing)).thenReturn(existing);

    userService.updateAddress(6L, request);

    verify(addressMapper).toEntity(request, existing);
    verify(addressRepository).save(existing);
  }

  @Test
  void updateAddress_rejectsMissingAddress() {
    when(addressRepository.findById(7L)).thenReturn(Optional.empty());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> userService.updateAddress(7L, AddressDTO.builder().build()))
        .withMessage("Address not found");
  }
}
