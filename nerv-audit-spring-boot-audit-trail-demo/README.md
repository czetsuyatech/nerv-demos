# NERV Audit demo

This Spring Boot application demonstrates the standard NERV Audit integration. Its only NERV Audit
dependency is `nerv-audit-spring-boot-starter`; the starter supplies Core, Operations, and the
optional Operations Web module transitively.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL running locally for the application profile

## Dependency

```xml
<dependency>
  <groupId>com.czetsuyatech.nerv</groupId>
  <artifactId>nerv-audit-spring-boot-starter</artifactId>
  <version>${nerv-audit.version}</version>
</dependency>
```

## Configuration

```yaml
nerv:
  audit:
    audit-insert: true
    audit-strategy-type: vertical
    operations:
      web:
        enabled: true
        base-path: /management/nerv-audit
```

The application persists an `@Audited` `UserEntity`. Operations Web is explicitly enabled so its
read-only inspection endpoints are available.

## Endpoints

- `POST /users`, `PUT /users/{id}`, `DELETE /users/{id}`, and `GET /users/{id}` are demo CRUD endpoints.
- `GET /management/nerv-audit/audits/vertical/{entity}` searches field-level audit records.
- `GET /management/nerv-audit/audits/horizontal/{entity}` retrieves Envers revision snapshots.

For example:

```text
GET /management/nerv-audit/audits/horizontal/UserEntity?size=20
GET /management/nerv-audit/audits/vertical/UserEntity?page=0&size=20
```

## Run and test

```bash
mvn spring-boot:run
mvn clean test
```

The H2-backed integration test proves that a starter-only consumer starts, persists an audited
entity, reads history through `AuditOperations`, and serves the enabled Operations Web endpoint.

## Current setup (NERV Audit 2.1.0)

Use an empty PostgreSQL schema named `nervaudit`. The vertical changelog creates the current audit tables directly
from NERV's official creation template, including timestamptz timestamps, required fields, constraints and indexes.
There is no legacy timestamp conversion or upgrade chain. Horizontal mode uses its own changelog; use a separate
empty schema/database when switching strategies.

Run `mvn -Pintegration-tests clean verify` to check vertical mode against PostgreSQL. Regular tests cover horizontal
mode on H2. Vertical search takes a fully qualified entity class name, such as
`com.czetsuyatech.nerv.audit.persistence.entity.UserEntity`; horizontal history is a separate API.
