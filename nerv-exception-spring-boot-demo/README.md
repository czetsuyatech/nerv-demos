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

## Next Steps

Planned additions:

* Kafka dead-letter queue demo
* Distributed tracing integration
* End-to-end integration scenarios
