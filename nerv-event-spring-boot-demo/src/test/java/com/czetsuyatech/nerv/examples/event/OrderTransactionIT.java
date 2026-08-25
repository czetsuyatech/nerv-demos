package com.czetsuyatech.nerv.examples.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.czetsuyatech.nerv.examples.event.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Acceptance test: the application writes its business row and starter-managed outbox atomically. */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
    "spring.main.web-application-type=none",
    "nerv.event.kafka.enabled=false",
    "nerv.event.sqs.enabled=false",
    "nerv.event.dispatcher.enabled=false",
    "nerv.event.inbox.dispatcher.enabled=false",
    "nerv.event.retention.enabled=false",
    "nerv.event.operations.web.enabled=false"
})
class OrderTransactionIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private OrderService orders;
  @Autowired private JdbcTemplate jdbc;

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Test
  void orderAndOutboxEventCommitTogetherAndBothRollbackTogether() {
    orders.createOrder("CUST-001");
    assertThat(count("demo_order")).isEqualTo(1);
    assertThat(count("nerv_outbox_event")).isEqualTo(1);

    assertThatThrownBy(() -> orders.createOrderThenFail("ROLLBACK"))
        .isInstanceOf(IllegalStateException.class);
    assertThat(count("demo_order")).isEqualTo(1);
    assertThat(count("nerv_outbox_event")).isEqualTo(1);
  }

  private int count(String table) {
    return jdbc.queryForObject("select count(*) from " + table, Integer.class);
  }
}
