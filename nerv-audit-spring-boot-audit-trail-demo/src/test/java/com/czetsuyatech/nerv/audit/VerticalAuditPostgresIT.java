package com.czetsuyatech.nerv.audit;

import static org.assertj.core.api.Assertions.assertThat;
import com.czetsuyatech.nerv.audit.application.dto.UserDTO;
import com.czetsuyatech.nerv.audit.application.query.AuditQuery;
import com.czetsuyatech.nerv.audit.operations.AuditOperations;
import com.czetsuyatech.nerv.audit.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class VerticalAuditPostgresIT {
  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.6")
      .withInitScript("init-audit-schema.sql");
  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "?currentSchema=nervaudit");
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
  @Autowired UserService users;
  @Autowired AuditOperations audits;
  @Autowired JdbcTemplate jdbc;

  @Test
  void verticalMigrationsSupportAuditWritesAndReads() {
    var created = users.create(UserDTO.builder().username("vertical-user").firstName("Before").build());
    users.update(created.getId(), UserDTO.builder().username("vertical-user").firstName("After").build());
    assertThat(audits.search("com.czetsuyatech.nerv.audit.persistence.entity.UserEntity", AuditQuery.builder().limit(50).build()).getContent()).isNotEmpty();
    assertThat(jdbc.queryForObject("select count(*) from user_account_aud where id=? and new_value='After'",
        Integer.class, created.getId())).isPositive();
    assertThat(jdbc.queryForList("select data_type from information_schema.columns where table_schema='nervaudit' "
        + "and table_name in ('user_account_aud','user_address_aud','user_hobby_aud') and column_name='updated'",
        String.class)).hasSize(3).containsOnly("timestamp with time zone");
  }
}
