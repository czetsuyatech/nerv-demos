# NERV Event Spring Boot Demo

This is an external-consumer acceptance demo for `nerv-event`. Its only primary NERV Event
dependency is `com.czetsuyatech.nerv:nerv-event-spring-boot-starter`. The deliberately separate
`nerv-event-operations-web` dependency supplies its optional management HTTP endpoints; no NERV
dispatcher, repository, producer, client, or other infrastructure bean is declared by this app.

## What it shows

```text
REST POST /orders
       |
   OrderService (@Transactional)
       |-- PostgreSQL demo_order
       `-- NERV EventPublisher -> PostgreSQL nerv_outbox_event
                                      |
                         orders-kafka | -> Kafka order-events -> Inbox -> OrderCreatedHandler
                                      |                                |
                         payments-sqs | <- SQS payment-events <- Outbox
                                                                       |
                                                   Inbox -> PaymentRequestedHandler
```

`OrderCreatedHandler` publishes `PaymentRequested`, so one order proves both routes. Broker
consumption is configured by NERV; the handlers implement only `EventHandler<T>` and have no
Kafka/SQS annotations or imports.

The demo registers two ordered `EventHandlerInterceptor` beans. `EventUserContextInterceptor` creates a simple
ThreadLocal execution context before each handler and restores the prior context in `finally`. `EventContextInterceptor`
snapshots MDC, adds the event ID, type, and optional correlation ID, then restores the complete prior MDC map in
`finally`. They demonstrate the same broker-neutral chain for Kafka, SQS, and scheduled Inbox retry executions without
affecting duplicate handling or acknowledgement behavior.

The app also demonstrates a named multi-client SQS configuration:

- `payments` routes through `account-a` to `payment-events`.
- `notifications` routes through `account-b` to `notification-events`.

Both accounts point at LocalStack for a simple local demo, but they are separate NERV client
identities. Business code never selects or calls an SQS client.

## Prerequisites

- Java 21 and Maven 3.9+
- Docker Compose
- A locally available `1.0.0` build of `nerv-event` (from its checkout run
  `mvn clean install` once)

The starter-owned LocalStack SQS clients use the standard AWS credentials provider. Set harmless
local values before launching the application:

```powershell
$env:AWS_ACCESS_KEY_ID = "test"
$env:AWS_SECRET_ACCESS_KEY = "test"
```

## Start and run

From this module:

```bash
docker compose up -d
mvn spring-boot:run
```

The application connects to PostgreSQL at `localhost:5432`, Kafka at `localhost:9092`, and
LocalStack at `localhost:4566`. Flyway runs the copied, immutable canonical NERV PostgreSQL
migrations as application-owned `V1`–`V4` migrations, then applies `V5__demo_order.sql` for the
business table. Hibernate is set to `validate`, never `update`.

## Create orders

Normal flow:

```bash
curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerId":"CUST-001"}'
```

Retryable payment flow:

```bash
curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerId":"RETRY-ONCE"}'
```

The payment handler throws public `EventRetryableException` once for this customer. Its durable
Inbox row transitions `PROCESSING -> RETRY_PENDING -> PROCESSING -> PROCESSED` through NERV's
normal `InboxRetryDispatcher`; it is not resent through Kafka or SQS.

Terminal failure:

```bash
curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"customerId":"FAIL-PERMANENT"}'
```

This handler throws a normal exception, creating a durable `FAILED` Inbox row. Ordinary orders do
not fail. To recover the same event manually, restart with
`--nerv.demo.allow-permanent-failure-recovery=true`, then retry the exact event ID below. The
manual retry changes the same durable row from `FAILED` to `RETRY_PENDING`; it does not replay or
create another Inbox row.

## Operations web and payload safety

Operations web is intentionally enabled at `/management/nerv-event`; payloads remain hidden by
default (`nerv.event.operations.web.payload.enabled=false`).

```bash
curl http://localhost:8080/management/nerv-event/outbox
curl http://localhost:8080/management/nerv-event/inbox?status=FAILED
curl -X POST http://localhost:8080/management/nerv-event/inbox/EVENT_ID/retry
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

Enable payloads only for controlled local troubleshooting with
`--nerv.event.operations.web.payload.enabled=true`. The starter provides NERV observability; this
demo merely exposes the standard Actuator health and metrics endpoints. Trace context can propagate
when the application has tracing enabled, but no collector or OTLP exporter is required to start it.

## Inspect PostgreSQL

```bash
docker compose exec postgres psql -U nerv -d nerv_event_demo
```

Use the actual NERV schema names:

```sql
SELECT * FROM nerv_outbox_event ORDER BY created_at DESC;
SELECT * FROM nerv_inbox_event ORDER BY received_at DESC;
SELECT * FROM nerv_inbox_event WHERE status = 'RETRY_PENDING';
SELECT * FROM nerv_inbox_event WHERE status = 'FAILED';
SELECT * FROM demo_order ORDER BY created_at DESC;
```

## Acceptance tests

The default build compiles the acceptance tests without requiring Docker. Run the PostgreSQL
transaction acceptance test (Testcontainers skips it automatically when Docker is unavailable):

```bash
mvn verify -Pintegration-tests
```

It verifies that `OrderService.createOrder()` commits an order and its starter-managed outbox row
together, while `createOrderThenFail()` rolls back both. The running Compose flow is the end-to-end
Kafka and SQS acceptance path; inspect the Inbox rows to verify `OrderCreated` and
`PaymentRequested` are `PROCESSED`.

Inspect the declared dependencies (the internals shown below the starter are transitive only):

```bash
mvn dependency:tree -Dincludes=com.czetsuyatech.nerv
```

## Stop and clean infrastructure

```bash
docker compose down -v
```
