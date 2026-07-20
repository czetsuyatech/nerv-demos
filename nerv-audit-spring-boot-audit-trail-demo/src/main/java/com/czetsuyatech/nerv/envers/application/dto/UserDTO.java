package com.czetsuyatech.nerv.envers.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private LocalDateTime birthDate;
  private List<String> hobbies;
  private List<AddressDTO> addresses;
}
