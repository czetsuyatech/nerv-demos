package com.czetsuyatech.nerv.audit.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.czetsuyatech.nerv.audit.application.dto.AddressDTO;
import com.czetsuyatech.nerv.audit.application.dto.UserDTO;
import com.czetsuyatech.nerv.audit.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Test
  void createUser_returnsCreatedUser() throws Exception {
    UserDTO user = UserDTO.builder()
        .id(1L)
        .username("alice")
        .firstName("Alice")
        .lastName("Smith")
        .build();
    when(userService.create(any(UserDTO.class))).thenReturn(user);

    mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"alice","firstName":"Alice","lastName":"Smith"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("alice"));
  }

  @Test
  void getUser_returnsUserWhenPresent() throws Exception {
    when(userService.getById(7L)).thenReturn(Optional.of(UserDTO.builder()
        .id(7L)
        .username("alice")
        .build()));

    mockMvc.perform(get("/users/7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.username").value("alice"));
  }

  @Test
  void getUser_returnsNotFoundWhenMissing() throws Exception {
    when(userService.getById(9L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/users/9"))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateUser_delegatesToService() throws Exception {
    mockMvc.perform(put("/users/3")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"username":"updated","firstName":"Updated","lastName":"User"}
                """))
        .andExpect(status().isOk());

    verify(userService).update(eq(3L), any(UserDTO.class));
  }

  @Test
  void updateAddress_delegatesToService() throws Exception {
    mockMvc.perform(put("/users/3/addresses/5")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"street":"1 Main Street","city":"Manila","country":"PH"}
                """))
        .andExpect(status().isOk());

    verify(userService).updateAddress(eq(5L), any(AddressDTO.class));
  }

  @Test
  void deleteUser_delegatesToService() throws Exception {
    mockMvc.perform(delete("/users/4"))
        .andExpect(status().isOk());

    verify(userService).delete(4L);
  }
}
