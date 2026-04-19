package com.czetsuyatech.envers.application.mapper;

import com.czetsuyatech.envers.application.dto.AddressDTO;
import com.czetsuyatech.envers.persistence.entity.AddressEntity;
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
    config = GlobalMapperConfig.class
)
public interface AddressMapper {


  @Mapping(source = "user.id", target = "userId")
  AddressDTO toAddressDTO(AddressEntity addressEntity);

  AddressEntity toEntity(AddressDTO addressDTO);

  @Mapping(target = "id", ignore = true)
  void toEntity(AddressDTO addressDTO, @MappingTarget AddressEntity addressEntity);

  default AddressEntity fromId(Long id) {

    if (id == null) {
      return null;
    }

    AddressEntity entity = new AddressEntity();
    entity.setId(id);
    return entity;
  }
}
