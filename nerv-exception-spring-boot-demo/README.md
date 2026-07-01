# NERV Exception Demo

A comprehensive demonstration of the `nerv-exception` ecosystem showcasing standardized error handling, distributed tracing, Feign integration, retry-aware error propagation, Kafka event publishing, and dead-letter queue processing in a Spring Boot application.

---

# Overview

Modern distributed systems often suffer from inconsistent error handling.

Different services return different payloads, expose different HTTP status codes, use different retry strategies, and provide little observability when failures occur.

`nerv-exception` solves this by providing:

* Standardized error responses
* Centralized exception handling
* Domain-specific error codes
* Retry-aware error propagation
* Distributed tracing support
* Feign integration
* Kafka error events
* Dead-letter queue support
* Cross-service error preservation

The goal is to make failures predictable, observable, and actionable across an entire microservice ecosystem.

---

# Architecture

```text
                    ┌───────────────────┐
                    │   HTTP Request    │
                    └─────────┬─────────┘
                              │
                              ▼
                 ┌─────────────────────────┐
                 │ NervExceptionHandler    │
                 └─────────┬───────────────┘
                           │
                           ▼
                 ┌─────────────────────────┐
                 │   NervErrorResponse     │
                 └─────────┬───────────────┘
                           │
                           ▼
                   ┌───────────────┐
                   │ OpenFeign     │
                   └───────┬───────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ NervFeignErrorDecoder│
                └───────┬──────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
        ▼                               ▼
 RetryableException         NervDownstreamException
        │                               │
        ▼                               ▼
 Resilience4j Retry           Standardized Error
        │
        ▼
 Success / Failure

---------------------------------------------------

                 Kafka Consumer
                        │
                        ▼
              NervKafkaErrorHandler
                        │
                        ▼
                 NervErrorEvent
                        │
                        ▼
                   DLQ Topic
```

---

# Technology Stack

* Java 21
* Spring Boot 4.1
* Spring Web
* Spring Cloud OpenFeign
* Micrometer Tracing
* OpenTelemetry
* Spring Kafka
* Resilience4j
* Maven

---

# Demo Modules

This demo showcases the following integrations:

| Feature            | Status |
| ------------------ | ------ |
| Spring Web         | ✅      |
| Feign              | ✅      |
| Retry              | ✅      |
| Micrometer Tracing | ✅      |
| OpenTelemetry      | ✅      |
| Kafka              | ✅      |
| DLQ Publishing     | ✅      |

---

# Core Concepts

## NervErrorCode

All application errors implement a common contract.

```java
public interface NervErrorCode {

    String code();

    String message();

    int status();

    boolean retryable();

    String category();
}
```

Example:

```java
public enum PaymentErrorCode implements NervErrorCode {

    PAYMENT_NOT_FOUND(
        "PAYMENT_NOT_FOUND",
        "Payment was not found",
        404,
        false,
        "BUSINESS"
    ),

    PAYMENT_TIMEOUT(
        "PAYMENT_TIMEOUT",
        "Payment provider timed out",
        504,
        true,
        "INTEGRATION"
    );
}
```

---

## Retryability

Retry behavior is part of the error contract.

```java
PAYMENT_TIMEOUT.retryable() == true
```

This allows downstream services to communicate whether an operation is safe to retry.

---

# Spring Web Integration

The Web module provides:

* Global exception handling
* Automatic HTTP status mapping
* Standardized response payloads
* Trace information
* Retryability metadata

No annotations are required.

Simply add:

```xml
<dependency>
    <groupId>com.czetsuyatech</groupId>
    <artifactId>nerv-exception-spring-boot-starter</artifactId>
</dependency>
```

---

## Standard Error Response

Example:

```json
{
  "code": "PAYMENT_TIMEOUT",
  "message": "Payment provider timed out",
  "status": 504,
  "retryable": true,
  "category": "INTEGRATION",
  "traceId": "0af7651916cd43dd8448eb211c80319c",
  "spanId": "b9c7c989f97918e1",
  "path": "/payments/timeout",
  "timestamp": "2026-06-23T00:00:00Z"
}
```

---

# Distributed Tracing

`nerv-exception` does not implement its own tracing system.

Instead it integrates with:

* Micrometer Tracing
* OpenTelemetry

Trace information is obtained from the active span.

```java
public interface NervTraceContextResolver {

    NervTraceContext current();
}
```

Default implementation:

```java
MicrometerNervTraceContextResolver
```

---

## OpenTelemetry Configuration

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
```

---

## Trace Propagation

Propagation is handled automatically by OpenTelemetry.

Headers:

```text
traceparent
tracestate
```

No custom propagation configuration is required.

---

# Feign Integration

The Feign module provides:

* Standardized remote error decoding
* Error code resolution
* Retry-aware exception conversion
* Downstream error preservation

---

## Feign Client

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
}
```

---

## Error Decoding

When a downstream service returns:

```json
{
  "code": "PAYMENT_TIMEOUT",
  "retryable": true
}
```

`NervFeignErrorDecoder` converts it into:

```java
RetryableException
```

When the error is non-retryable:

```java
NervDownstreamException
```

is created instead.

---

# Downstream Error Preservation

Remote failures preserve the original context.

Example:

```json
{
  "code": "PAYMENT_TIMEOUT",
  "traceId": "abc123",
  "spanId": "xyz789"
}
```

This information remains available after Feign decoding.

Preserved metadata includes:

* traceId
* spanId
* timestamp
* path
* details

This makes cross-service troubleshooting significantly easier.

---

# Retry Integration

Retry policies are intentionally externalized.

The library communicates:

```java
retryable() == true
```

Applications decide:

* Retry count
* Backoff strategy
* Circuit breaker behavior
* Timeout policy

---

## Resilience4j Example

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot4</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

Configuration:

```yaml
resilience4j:
  retry:
    instances:
      paymentProvider:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - feign.RetryableException
```

Service:

```java
@Service
@RequiredArgsConstructor
public class PaymentRetryService {

    private final PaymentProviderClient client;

    @Retry(name = "paymentProvider")
    public String processPayment() {
        return client.timeout();
    }
}
```

---

# Error Code Registry

Applications may register custom error code enums.

```java
@Bean
NervErrorCodeRegistry applicationErrorCodeRegistry() {
    return new EnumNervErrorCodeRegistry(
        PaymentErrorCode.values(),
        CustomerErrorCode.values(),
        OrderErrorCode.values()
    );
}
```

Resolution order:

```text
Application Registry
        ↓
Native Registry
```

Built-in errors remain available through:

```java
NativeNervErrorCodes
```

Duplicate error codes are detected during startup.

---

# Kafka Integration

Kafka support transforms processing failures into structured events.

Features:

* Error event generation
* DLQ publishing
* Trace preservation
* Error metadata headers
* Async error processing

---

## Message Flow

```text
Producer
    ↓
payments
    ↓
Consumer
    ↓
NervException
    ↓
NervKafkaErrorHandler
    ↓
NervErrorEvent
    ↓
payments.dlq
```

---

## Example Event

```json
{
  "code": "PAYMENT_TIMEOUT",
  "message": "Payment provider timed out",
  "category": "INTEGRATION",
  "retryable": true,
  "source": "nerv-exception-demo",
  "traceId": "0af7651916cd43dd8448eb211c80319c",
  "spanId": "b9c7c989f97918e1",
  "parentEventId": "payment-timeout-failed"
}
```

---

## Kafka Headers

Published headers:

```text
nerv-trace-id
nerv-span-id
nerv-source
nerv-parent-event-id
nerv-error-code
nerv-error-category
```

---

# Demo Endpoints

## Web

```http
GET /payments/123
GET /payments/404
GET /payments/timeout
GET /payments/error
```

## Feign

```http
GET /feign/payments/not-found
GET /feign/payments/timeout
GET /feign/trace
```

## Retry

```http
GET /feign/retry/reset
GET /feign/retry/timeout
GET /feign/retry/attempts
```

## Kafka

```http
POST /kafka/payments/{id}
POST /kafka/payments/{id}/fail
```

---

# Design Principles

`nerv-exception` follows several core principles:

1. Error codes are first-class citizens.
2. Retryability belongs to the error contract.
3. Tracing is delegated to Micrometer/OpenTelemetry.
4. Retry frameworks remain application concerns.
5. Error handling should be consistent across HTTP, Feign, and Kafka.
6. Cross-service diagnostics should preserve original failure context.
7. Infrastructure concerns should remain pluggable and optional.

---

# Repository Structure

```text
nerv-exception-api
nerv-exception-core
nerv-exception-spring-web
nerv-exception-spring-feign
nerv-exception-event
nerv-exception-spring-kafka
nerv-exception-spring-boot-starter

nerv-exception-demo
```

---

# What This Demo Proves

This demo demonstrates:

* Standardized HTTP error handling
* Retry-aware Feign integration
* OpenTelemetry-based distributed tracing
* Cross-service error preservation
* Kafka error event publishing
* Dead-letter queue processing
* Resilience4j retry integration
* Custom error code registration

Together these capabilities provide a consistent failure-management strategy across synchronous and asynchronous communication channels.
