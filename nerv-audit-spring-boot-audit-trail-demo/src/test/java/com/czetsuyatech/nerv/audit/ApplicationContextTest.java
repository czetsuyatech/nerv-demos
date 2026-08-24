package com.czetsuyatech.nerv.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.czetsuyatech.nerv.audit.application.query.AuditQuery;
import com.czetsuyatech.nerv.audit.service.AuditService;
import com.czetsuyatech.nerv.audit.application.dto.UserDTO;
import com.czetsuyatech.nerv.audit.operations.AuditOperations;
import com.czetsuyatech.nerv.audit.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:nerv_audit_demo;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=PUBLIC",
    "spring.liquibase.enabled=false",
    "nerv.audit.audit-strategy-type=HORIZONTAL",
    "nerv.audit.operations.web.enabled=true"
})
class ApplicationContextTest {

  @Autowired
  private UserService userService;

  @Autowired
  private AuditService auditService;

  @Autowired
  private AuditOperations auditOperations;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void starterOnlyConsumerStartsAuditsEntitiesAndExposesOperations() throws Exception {
    assertThat(userService).isNotNull();
    assertThat(auditService).isNotNull();
    assertThat(auditOperations).isNotNull();

    UserDTO created = userService.create(UserDTO.builder()
        .username("integration-user")
        .firstName("Integration")
        .lastName("Test")
        .build());

    assertThat(created.getId()).isNotNull();
    assertThat(userService.getById(created.getId()))
        .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("integration-user"));
    assertThat(auditOperations.history("UserEntity", AuditQuery.builder().limit(5).build()))
        .isNotEmpty();

    mockMvc.perform(get("/management/nerv-audit/audits/horizontal/UserEntity").param("size", "5"))
        .andExpect(status().isOk());
  }
}
