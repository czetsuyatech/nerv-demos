package com.czetsuyatech.envers.service;

import com.czetsuyatech.envers.persistence.entity.UserEntity;
import java.util.Optional;

public interface UserService {

  UserEntity create();

  void update(Long userId);

  void delete(Long userId);

  Optional<UserEntity> getById(Long userId);

  Object getHorizontalRevisions(Long userId);

  Object getVerticalRevisions(Long userId);
}
