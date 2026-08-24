package com.czetsuyatech.nerv.envers;

import static org.assertj.core.api.Assertions.assertThat;

import com.czetsuyatech.nerv.audit.service.AuditService;
import com.czetsuyatech.nerv.envers.application.dto.UserDTO;
import com.czetsuyatech.nerv.envers.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:nerv_audit_demo;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=PUBLIC",
    "spring.liquibase.enabled=false",
    "nerv.audit.audit-strategy-type=HORIZONTAL",
    "nerv.audit.web.enabled=false"
})
class ApplicationContextTest {

  @Autowired
  private UserService userService;

  @Autowired
  private AuditService auditService;

  @Test
  void applicationStartsAndPersistsAUserThroughTheDemoService() {
    assertThat(userService).isNotNull();
    assertThat(auditService).isNotNull();

    UserDTO created = userService.create(UserDTO.builder()
        .username("integration-user")
        .firstName("Integration")
        .lastName("Test")
        .build());

    assertThat(created.getId()).isNotNull();
    assertThat(userService.getById(created.getId()))
        .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("integration-user"));
  }
}
