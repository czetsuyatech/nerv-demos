# Verification — 2026-09-14

Java 21, Spring Boot 4.1.0; NERV Audit/Event 2.1.0 and Exception 1.4.0 from locally resolved artifacts.

`mvn -o -Pintegration-tests clean verify` passed across all three modules:

- 20 unit/application-context tests; no failures, errors or skips.
- 6 integration tests; no failures, errors or skips.
- Audit: horizontal mode on H2; real PostgreSQL fresh vertical schema initialization, default schema validation, audit writes and reads.
- Event: PostgreSQL outbox/business rollback, persisted ordering key, lease fencing, Kafka-to-SQS processing,
  transient retries and operations-based recovery of permanent failures.
- Exception: full application startup and HTTP success/not-found/timeout/internal-error mappings with Kafka listeners
  disabled for the test. Exception-specific Kafka and Feign flows were not exercised by this run.

The demos target a fresh database. Audit creates vertical tables directly from NERV's current creation template;
Event uses one initial schema containing claim fencing and ordering keys. No historical upgrade chain is retained.
Integration tests use isolated empty databases. Existing developer database contents are not reset automatically.

Event polling is shortened only in integration tests. Assertions identify the current order, and Docker absence fails
integration verification rather than silently skipping it. Event Flyway now explicitly owns the nervevent schema; the
Compose database health check targets the configured nerv_examples database.

Exception retains Boot 4.1 / Cloud 2025.1.2 with an explicit compatibility-verifier override. The HTTP context test is
not a guarantee for every Spring Cloud feature. Runtime image reduction and production rollout were not part of this
verification. No sibling NERV library implementation was changed.

Fresh-schema follow-up: the clean full suite passed again after removing the upgrade chain (20 unit/context tests and
6 integration tests, no failures or skips). Audit uses the official table creation template; Event creates its final
2.1.0 columns and indexes in one initial file. Liquibase/Flyway remain only to initialize the demo automatically.
