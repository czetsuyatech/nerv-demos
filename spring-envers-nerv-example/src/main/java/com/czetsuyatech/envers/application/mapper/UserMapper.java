package com.czetsuyatech.envers.application.mapper;

import com.czetsuyatech.envers.application.dto.UserDTO;
import com.czetsuyatech.envers.persistence.entity.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    uses = {AddressMapper.class}
)
public interface UserMapper {

  UserDTO toDto(UserEntity userEntity);

  UserEntity toEntity(UserDTO userDTO);

  @Mapping(target = "id", ignore = true)
  UserEntity toEntity(UserDTO userDTO, @MappingTarget UserEntity entity);

  @AfterMapping
  default void setAddressesUser(@MappingTarget UserEntity entity) {

    if (entity.getAddresses() != null) {
      entity.getAddresses().forEach(address -> address.setUser(entity));
    }
  }

  default UserEntity fromId(Long id) {

    if (id == null) {
      return null;
    }

    UserEntity entity = new UserEntity();
    entity.setId(id);
    return entity;
  }
}
