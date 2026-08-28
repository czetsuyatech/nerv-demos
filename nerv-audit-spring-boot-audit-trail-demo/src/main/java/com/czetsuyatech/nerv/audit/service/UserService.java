package com.czetsuyatech.nerv.audit.service;

import com.czetsuyatech.nerv.audit.application.dto.AddressDTO;
import com.czetsuyatech.nerv.audit.application.dto.UserDTO;
import java.util.Optional;

public interface UserService {

  UserDTO create(UserDTO userDTO);

  void update(Long userId, UserDTO userDTO);

  void delete(Long userId);

  Optional<UserDTO> getById(Long userId);

  void updateAddress(Long addressId, AddressDTO addressDTO);
}
