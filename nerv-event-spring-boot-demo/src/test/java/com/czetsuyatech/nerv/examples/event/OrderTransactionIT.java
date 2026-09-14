package com.czetsuyatech.nerv.examples.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.czetsuyatech.nerv.examples.event.order.OrderService;
import com.czetsuyatech.nerv.event.core.outbox.OutboxEvent;
import com.czetsuyatech.nerv.event.core.outbox.OutboxService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
@Testcontainers
@SpringBootTest(properties = {
    "spring.main.web-application-type=none",
    "nerv.event.kafka.enabled=false",
    "nerv.event.sqs.enabled=false",
    "nerv.event.dispatcher.enabled=false",
    "nerv.event.dispatcher.lease-duration=1s",
    "nerv.event.inbox.dispatcher.enabled=false",
    "nerv.event.retention.enabled=false",
    "nerv.event.operations.web.enabled=false"
})
class OrderTransactionIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private OrderService orders;
  @Autowired private OutboxService outbox;
  @Autowired private JdbcTemplate jdbc;

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void clearTables() {
    jdbc.update("delete from demo_order");
    jdbc.update("delete from nerv_outbox_event");
  }

  @Test
  void orderAndOutboxEventCommitTogetherAndBothRollbackTogether() {
    var created = orders.createOrder("CUST-001");
    assertThat(jdbc.queryForObject("select ordering_key from nerv_outbox_event", String.class))
        .isEqualTo(created.id().toString());
    assertThat(count("demo_order")).isEqualTo(1);
    assertThat(count("nerv_outbox_event")).isEqualTo(1);

    assertThatThrownBy(() -> orders.createOrderThenFail("ROLLBACK"))
        .isInstanceOf(IllegalStateException.class);
    assertThat(count("demo_order")).isEqualTo(1);
    assertThat(count("nerv_outbox_event")).isEqualTo(1);
  }

  @Test
  void expiredLeaseReclaimFencesTheStaleWorker() {
    orders.createOrder("LEASE-FENCING");

    OutboxEvent staleClaim = onlyClaim();
    assertThat(staleClaim.claimVersion()).isEqualTo(1);

    jdbc.update(
        "update nerv_outbox_event set locked_at = ? where id = ?",
        Timestamp.from(Instant.now().minusSeconds(60)),
        staleClaim.id().value()
    );

    OutboxEvent currentClaim = onlyClaim();
    assertThat(currentClaim.claimVersion()).isEqualTo(2);
    assertThat(outbox.reschedule(
        staleClaim.id(),
        staleClaim.claimVersion(),
        1,
        Instant.now(),
        "stale worker"
    )).isFalse();
    assertThat(status(staleClaim.id().value())).isEqualTo("PROCESSING");
    assertThat(claimVersion(staleClaim.id().value())).isEqualTo(2);

    assertThat(outbox.reschedule(
        currentClaim.id(),
        currentClaim.claimVersion(),
        1,
        Instant.now(),
        "current worker"
    )).isTrue();
    assertThat(status(currentClaim.id().value())).isEqualTo("PENDING");
  }

  private OutboxEvent onlyClaim() {
    List<OutboxEvent> claimed = outbox.claimPending(Instant.now(), 10);
    assertThat(claimed).hasSize(1);
    return claimed.getFirst();
  }

  private String status(String id) {
    return jdbc.queryForObject("select status from nerv_outbox_event where id = ?", String.class, id);
  }

  private long claimVersion(String id) {
    return jdbc.queryForObject("select claim_version from nerv_outbox_event where id = ?", Long.class, id);
  }

  private int count(String table) {
    return jdbc.queryForObject("select count(*) from " + table, Integer.class);
  }
}
