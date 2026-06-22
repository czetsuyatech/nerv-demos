# NERV | Exception | Demos

Demo application showcasing the capabilities of `nerv-exception` in a Spring Boot application.

Current integrations:

* Spring Web
* OpenFeign (coming soon)
* Kafka (coming soon)

---

## Prerequisites

* Java 17+
* Maven 3.9+
* Spring Boot 3.x

---

## Running the Demo

Build and start the application:

```bash
mvn clean spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## Features Demonstrated

### Spring Web Integration

The demo shows how `nerv-exception` provides:

* Centralized exception handling
* Standardized error responses
* Custom application error codes
* Automatic HTTP status mapping
* Retryability metadata
* Error categorization

No additional configuration or annotations are required.

---

## Demo Error Codes

The application defines the following error codes:

| Code                 | HTTP Status | Retryable | Category      |
|----------------------|------------:|-----------|---------------|
| `PAYMENT_NOT_FOUND`  |         404 | No        | `BUSINESS`    |
| `PAYMENT_TIMEOUT`    |         504 | Yes       | `INTEGRATION` |
| `UNEXPECTED_FAILURE` |         500 | No        | `SYSTEM`      |

---

## Endpoints

### Successful Request

```http
GET /payments/123
```

Response:

```text
Payment found: 123
```

---

### Payment Not Found

```http
GET /payments/404
```

Response:

```http
HTTP/1.1 404 Not Found
```

```json
{
  "code": "PAYMENT_NOT_FOUND",
  "message": "Payment was not found",
  "category": "BUSINESS",
  "retryable": false,
  "traceId": "7fa3bc52f4b14c5f"
}
```

---

### Payment Timeout

```http
GET /payments/timeout
```

Response:

```http
HTTP/1.1 504 Gateway Timeout
```

```json
{
  "code": "PAYMENT_TIMEOUT",
  "message": "Payment provider timed out",
  "category": "INTEGRATION",
  "retryable": true,
  "traceId": "7fa3bc52f4b14c5f"
}
```

---

### Unexpected Error

```http
GET /payments/error
```

Response:

```http
HTTP/1.1 500 Internal Server Error
```

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "category": "SYSTEM",
  "retryable": false,
  "traceId": "7fa3bc52f4b14c5f"
}
```

---

## Project Structure

```text
src/main/java/com/czetsuyatech/nerv/examples/exception
├── NervExceptionDemoApplication.java
├── controller
│   └── PaymentController.java
└── error
    └── DemoErrorCode.java
```

---

## Dependency

The demo uses:

```xml

<dependency>
  <groupId>com.czetsuyatech</groupId>
  <artifactId>nerv-exception-spring-boot-starter</artifactId>
  <version>${nerv-exception.version}</version>
</dependency>
```

---

## Feign Integration

This demo showcases how `nerv-exception` integrates with OpenFeign to provide:

* Automatic trace propagation
* Real OpenTelemetry integration
* Standardized remote error handling
* Automatic conversion of remote errors into `NervException`

No additional Feign configuration is required.

### Dependencies

```xml
<dependency>
    <groupId>com.czetsuyatech</groupId>
    <artifactId>nerv-exception-spring-boot-starter</artifactId>
    <version>${nerv-exception.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```

### Configuration

Enable Feign and `nerv-exception` integration:

```yaml
spring:
  application:
    name: nerv-exception-demo

nerv:
  exception:
    web:
      enabled: true
    feign:
      enabled: true
```

Enable Feign clients:

```java
@SpringBootApplication
@EnableFeignClients
public class NervExceptionDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NervExceptionDemoApplication.class, args);
    }
}
```

### Trace Context Integration

The demo provides a custom `NervTraceContextResolver` backed by Micrometer Tracing.

```java
@Configuration
@RequiredArgsConstructor
public class DemoTraceConfiguration {

    private final Tracer tracer;

    @Bean
    NervTraceContextResolver nervTraceContextResolver() {
        return new NervTraceContextResolver() {

            @Override
            public String traceId() {
                Span span = tracer.currentSpan();
                return span == null ? null : span.context().traceId();
            }

            @Override
            public String spanId() {
                Span span = tracer.currentSpan();
                return span == null ? null : span.context().spanId();
            }
        };
    }
}
```

### Feign Client

```java
@FeignClient(
    name = "payment-provider",
    url = "${demo.provider.url}"
)
public interface PaymentProviderClient {

    @GetMapping("/provider/payments/not-found")
    String notFound();

    @GetMapping("/provider/payments/timeout")
    String timeout();

    @GetMapping("/provider/trace")
    Map<String, String> trace();
}
```

### Automatic Trace Propagation

`NervFeignTracePropagationInterceptor` automatically propagates:

* `nerv-trace-id`
* `nerv-span-id`

to downstream services.

### Verify Trace Propagation

Invoke:

```bash
curl http://localhost:8080/feign/trace
```

Example response:

```json
{
  "traceId": "0af7651916cd43dd8448eb211c80319c",
  "spanId": "b9c7c989f97918e1"
}
```

Expected behavior:

* The `traceId` remains the same across the request chain.
* The downstream service receives the propagated trace context.
* The `spanId` represents the current operation.

### Verify Error Decoding

Invoke:

```bash
curl http://localhost:8080/feign/payments/not-found
```

Response:

```http
HTTP/1.1 404 Not Found
```

```json
{
  "code": "PAYMENT_NOT_FOUND",
  "message": "Payment was not found",
  "category": "BUSINESS",
  "retryable": false,
  "traceId": "0af7651916cd43dd8448eb211c80319c"
}
```

Invoke:

```bash
curl http://localhost:8080/feign/payments/timeout
```

Response:

```http
HTTP/1.1 504 Gateway Timeout
```

```json
{
  "code": "PAYMENT_TIMEOUT",
  "message": "Payment provider timed out",
  "category": "INTEGRATION",
  "retryable": true,
  "traceId": "0af7651916cd43dd8448eb211c80319c"
}
```

### What Happens Internally

1. The incoming HTTP request creates a trace.
2. `NervTraceContextResolver` retrieves the active trace context.
3. `NervFeignTracePropagationInterceptor` adds trace headers to the outgoing Feign request.
4. The downstream service receives the trace headers.
5. If the downstream service returns an error response, `NervFeignErrorDecoder` converts it into a `NervException`.
6. `NervExceptionHandler` converts the exception into a standardized HTTP response.

This ensures consistent tracing and error handling across service boundaries.

---

## Event and Kafka Integration

This demo showcases how `nerv-exception` integrates with Kafka to convert failed message processing into standardized error events.

It demonstrates:

* Publishing normal Kafka messages
* Consuming Kafka messages
* Mapping failed processing into `NervErrorEvent`
* Publishing failed messages to a DLQ topic
* Propagating error metadata through Kafka headers
* Separating synchronous trace handling from asynchronous event trace handling

---

## Dependencies

```xml
<dependency>
    <groupId>com.czetsuyatech</groupId>
    <artifactId>nerv-exception-spring-boot-starter</artifactId>
    <version>${nerv-exception.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

---

## Kafka Configuration

```yaml
nerv:
  exception:
    kafka:
      enabled: true
      source: nerv-exception-demo
      dlq-topic-suffix: .dlq

spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer

    consumer:
      group-id: nerv-exception-demo
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JacksonJsonDeserializer
      properties:
        spring.json.trusted.packages: "com.czetsuyatech.nerv.example.exception.kafka,com.czetsuyatech.nerv.exception.event"
        spring.json.value.default.type: com.czetsuyatech.nerv.example.exception.kafka.PaymentMessage
```

For Spring Boot 4 / Spring Kafka 4, use `JacksonJsonSerializer` and `JacksonJsonDeserializer`.

---

## Local Kafka

```yaml
services:
  kafka:
    image: apache/kafka:4.0.1
    container_name: nerv-exception-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093

      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT

      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

Start Kafka:

```bash
docker compose up -d
```

---

## Message Model

```java
public record PaymentMessage(
    String paymentId,
    String status
) {
}
```

---

## Producer

```java
@Service
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentMessage> kafkaTemplate;

    public void publish(PaymentMessage message) {
        kafkaTemplate.send("payments", message.paymentId(), message);
    }
}
```

---

## Consumer

```java
@Component
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private final NervKafkaErrorHandler errorHandler;

    @KafkaListener(
        topics = "payments",
        groupId = "nerv-exception-demo"
    )
    public void consume(
        PaymentMessage message,
        ConsumerRecord<String, PaymentMessage> record
    ) {
        try {
            if ("FAIL".equals(message.status())) {
                throw new NervException(PaymentErrorCode.PAYMENT_TIMEOUT);
            }

            System.out.println("Payment processed: " + message.paymentId());
        } catch (Exception ex) {
            errorHandler.handle(record, ex);
        }
    }
}
```

---

## DLQ Consumer

```java
@Component
public class PaymentKafkaDlqConsumer {

    @KafkaListener(
        topics = "payments.dlq",
        groupId = "nerv-exception-demo-dlq"
    )
    public void consumeDlq(
        NervErrorEvent event,
        ConsumerRecord<String, NervErrorEvent> record
    ) {
        System.out.println("DLQ event received:");
        System.out.println(event);

        record.headers().forEach(header ->
            System.out.println(header.key() + "=" + new String(header.value()))
        );
    }
}
```

---

## Demo Controller

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/kafka/payments")
public class PaymentKafkaDemoController {

    private final PaymentKafkaProducer producer;

    @PostMapping("/{id}")
    public String publish(@PathVariable String id) {
        producer.publish(new PaymentMessage(id, "PROCESSING"));
        return "Payment message published: " + id;
    }

    @PostMapping("/{id}/fail")
    public String publishFailing(@PathVariable String id) {
        producer.publish(new PaymentMessage(id, "FAIL"));
        return "Failing payment message published: " + id;
    }
}
```

---

## Successful Message Flow

Request:

```bash
curl -X POST http://localhost:8080/kafka/payments/123
```

Flow:

```text
POST /kafka/payments/123
    ↓
PaymentKafkaProducer
    ↓
payments topic
    ↓
PaymentKafkaConsumer
    ↓
Payment processed successfully
```

---

## Failed Message Flow

Request:

```bash
curl -X POST http://localhost:8080/kafka/payments/timeout/fail
```

Flow:

```text
POST /kafka/payments/timeout/fail
    ↓
PaymentKafkaProducer
    ↓
payments topic
    ↓
PaymentKafkaConsumer
    ↓
NervException(PAYMENT_TIMEOUT)
    ↓
NervKafkaErrorHandler
    ↓
NervErrorEvent
    ↓
payments.dlq
```

---

## DLQ Topic Resolution

The DLQ topic is resolved from the original topic plus the configured suffix.

```text
original topic: payments
suffix:         .dlq
DLQ topic:      payments.dlq
```

Configured by:

```yaml
nerv:
  exception:
    kafka:
      dlq-topic-suffix: .dlq
```

---

## Kafka Headers

`NervKafkaHeaderMapper` maps error metadata into Kafka headers.

Expected headers:

| Header                 | Description             |
| ---------------------- | ----------------------- |
| `nerv-trace-id`        | Trace identifier        |
| `nerv-span-id`         | Span identifier         |
| `nerv-source`          | Source application      |
| `nerv-parent-event-id` | Parent event identifier |
| `nerv-error-code`      | Error code              |
| `nerv-error-category`  | Error category          |

Example output:

```text
nerv-trace-id=0af7651916cd43dd8448eb211c80319c
nerv-span-id=b9c7c989f97918e1
nerv-source=nerv-exception-demo
nerv-parent-event-id=payment-timeout-failed
nerv-error-code=PAYMENT_TIMEOUT
nerv-error-category=INTEGRATION
```

---

## Example `NervErrorEvent`

```json
{
  "code": "PAYMENT_TIMEOUT",
  "message": "Payment provider timed out",
  "category": "INTEGRATION",
  "retryable": true,
  "source": "nerv-exception-demo",
  "traceId": "0af7651916cd43dd8448eb211c80319c",
  "spanId": "b9c7c989f97918e1",
  "parentEventId": "payment-timeout-failed",
  "timestamp": "2026-06-22T10:15:30Z"
}
```

---

## What Happens Internally

1. A payment message is published to the `payments` topic.
2. `PaymentKafkaConsumer` receives the message.
3. If the message status is `FAIL`, the consumer throws a `NervException`.
4. The exception is passed to `NervKafkaErrorHandler`.
5. `NervKafkaErrorHandler` maps the exception into a `NervErrorEvent`.
6. `NervKafkaDlqPublisher` publishes the event to `payments.dlq`.
7. `NervKafkaHeaderMapper` writes error metadata into Kafka headers using `ProducerRecord`.
8. `PaymentKafkaDlqConsumer` receives the DLQ event and prints the event plus headers.

---

## Why `ProducerRecord` Is Used

Kafka headers must be attached to a `ProducerRecord`.

`KafkaTemplate.send()` does not expose a direct overload for sending arbitrary headers with only topic, key, and value.

So `NervKafkaDlqPublisher` uses:

```java
ProducerRecord<String, NervErrorEvent> record =
    new ProducerRecord<>(dlqTopic, key, event);

Headers headers = headerMapper.from(event);

headers.forEach(record.headers()::add);

return kafkaTemplate.send(record)
    .thenApply(result -> null);
```

This ensures the DLQ message contains both:

* structured `NervErrorEvent` payload
* Kafka headers for routing, observability, and diagnostics


---

## Next Steps

Planned additions:

* Kafka dead-letter queue demo
* Distributed tracing integration
* End-to-end integration scenarios
