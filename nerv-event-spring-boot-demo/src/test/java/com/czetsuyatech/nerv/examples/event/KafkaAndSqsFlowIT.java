package com.czetsuyatech.nerv.examples.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.czetsuyatech.nerv.examples.event.payment.PaymentFailureSwitch;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/** Full external-consumer path: REST -> outbox -> Kafka -> inbox -> SQS -> inbox. */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class KafkaAndSqsFlowIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
  @Container static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));
  @Container static LocalStackContainer localstack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:4.10")).withServices("sqs");

  @Autowired private JdbcTemplate jdbc;
  @Autowired private PaymentFailureSwitch failureSwitch;
  @LocalServerPort private int port;

  @BeforeAll
  static void awsCredentials() {
    System.setProperty("aws.accessKeyId", "test");
    System.setProperty("aws.secretAccessKey", "test");
  }

  @DynamicPropertySource
  static void infrastructure(DynamicPropertyRegistry registry) {
    createQueue("payment-events");
    createQueue("notification-events");
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("nerv.event.sqs.clients.account-a.endpoint",
        () -> localstack.getEndpoint().toString());
    registry.add("nerv.event.sqs.clients.account-b.endpoint",
        () -> localstack.getEndpoint().toString());
  }

  @Test
  void kafkaThenSqsProducesProcessedInboxRows() {
    create("CUST-" + UUID.randomUUID());
    waitFor("payment.requested", "PROCESSED");
    assertThat(statusFor("order.created")).isEqualTo("PROCESSED");
  }

  @Test
  void retryablePaymentUsesInboxRetryInsteadOfBrokerRedelivery() {
    create("RETRY-ONCE");
    waitFor("payment.requested", "PROCESSED");
    assertThat(attemptsFor("payment.requested")).isEqualTo(2);
  }

  @Test
  void failedPaymentCanBeRecoveredThroughOperationsWebWithTheSameEventId() {
    create("FAIL-PERMANENT");
    waitFor("payment.requested", "FAILED");
    String eventId = eventIdFor("payment.requested");
    failureSwitch.allowPermanentFailureRecovery();
    assertThat(post("/management/nerv-event/inbox/" + eventId + "/retry", "")).isEqualTo(202);
    waitFor("payment.requested", "PROCESSED");
    assertThat(eventIdFor("payment.requested")).isEqualTo(eventId);
    assertThat(countForEvent(eventId)).isEqualTo(1);
  }

  private void create(String customerId) {
    assertThat(post("/orders", "{\"customerId\":\"" + customerId + "\"}")).isEqualTo(201);
  }

  private void waitFor(String type, String expectedStatus) {
    Instant deadline = Instant.now().plus(Duration.ofSeconds(30));
    while (Instant.now().isBefore(deadline)) {
      String actual = statusFor(type);
      if (expectedStatus.equals(actual)) return;
      try { Thread.sleep(200); } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while awaiting " + type, exception);
      }
    }
    assertThat(statusFor(type)).isEqualTo(expectedStatus);
  }

  private String statusFor(String type) {
    try {
      return jdbc.queryForObject("select status from nerv_inbox_event where event_type = ? order by received_at desc limit 1",
          String.class, type);
    } catch (EmptyResultDataAccessException ignored) {
      return null;
    }
  }

  private int attemptsFor(String type) {
    return jdbc.queryForObject("select attempt_count from nerv_inbox_event where event_type = ? order by received_at desc limit 1",
        Integer.class, type);
  }

  private String eventIdFor(String type) {
    return jdbc.queryForObject("select event_id from nerv_inbox_event where event_type = ? order by received_at desc limit 1",
        String.class, type);
  }

  private int countForEvent(String eventId) {
    return jdbc.queryForObject("select count(*) from nerv_inbox_event where event_id = ?", Integer.class, eventId);
  }

  private String url(String path) { return "http://localhost:" + port + path; }

  private int post(String path, String body) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();
      return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    } catch (Exception exception) {
      throw new IllegalStateException("HTTP request failed: " + path, exception);
    }
  }

  private static void createQueue(String queue) {
    try (SqsAsyncClient client = SqsAsyncClient.builder()
        .endpointOverride(localstack.getEndpoint())
        .region(Region.of(localstack.getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
        .build()) {
      client.createQueue(request -> request.queueName(queue)).join();
    }
  }
}
