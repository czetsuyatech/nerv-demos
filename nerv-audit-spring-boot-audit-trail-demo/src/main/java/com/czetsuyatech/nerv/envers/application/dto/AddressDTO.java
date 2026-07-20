package com.czetsuyatech.nerv.envers.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

  private Long id;
  private String street;
  private String city;
  private String country;
  private Long userId;
}
