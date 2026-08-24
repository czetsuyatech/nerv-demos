# NERV | Audit | Demos

This project demonstrates how to use `nerv-audit-spring-boot-starter` with Spring Boot + JPA + Envers + Liquibase.

It includes examples for:
- Horizontal auditing
- Vertical auditing
- Create, update, delete, and collection auditing with revision endpoints

## 1. What this example contains

- Audit-enabled JPA entity: `UserEntity` (`@Audited`)
- CRUD service operations: 
  - create/update/delete/get
- Revision endpoints:
  - `GET /nerv-audit/horizontal/UserEntity?offset=2&limit=10`
  - `GET /nerv-audit/vertical/com.czetsuyatech.envers.persistence.entity.UserEntity?offset=2&limit=2`
- Liquibase changelogs for both audit strategies:
  - `db.horizontal-changelog-master.xml`
  - `db.vertical-changelog-master.xml`

## 2. Prerequisites

- Java 25
- Maven 3.9+
- PostgreSQL running locally
- Database: `nerv-examples`
- Schema: `nervaudit`
- Username/Password: `nerv` / `nerv`

## 3. Dependency (nerv-audit)

Already configured in `pom.xml`:

```xml
<dependency>
  <groupId>com.czetsuyatech.nerv</groupId>
  <artifactId>nerv-audit</artifactId>
  <version>1.2.0</version>
</dependency>
```

## 4. Configuration

`src/main/resources/application.yml`:

```yaml
nerv:
  audit:
    web:
      enabled: true
    audit-insert: true
    audit-strategy-type: vertical # change to horizontal if needed

spring:
  liquibase:
    change-log: classpath:db/changelog/db.${nerv.audit.audit-strategy-type}-changelog-master.xml
```

Key points:
- `audit-strategy-type` controls whether horizontal or vertical changelog is used.
- You can switch strategy by changing one property value.

## 5. Horizontal vs Vertical auditing

### Horizontal auditing

- Stores the full row snapshot per revision.
- Uses audit table like `user_account_aud` with entity columns (`first_name`, `last_name`, `birth_date`, etc.).
- Better when you need full point-in-time state reconstruction quickly.

Liquibase file used:
- `src/main/resources/db/changelog/audit/horizontal/002-user-audit-tables.xml`

### Vertical auditing

- Stores field-level changes (`field_name`, `old_value`, `new_value`).
- Better when you need change history per field and diff-style traces.

Liquibase file used:
- `src/main/resources/db/changelog/audit/vertical/002-user-audit-tables.xml`

## 6. Create and revision endpoint example

Use this when you need auditing generated from JPA operations, with revision APIs.

Minimum requirements:
1. Add the `nerv-audit` dependency.
2. Mark entities with `@Audited`.
3. Persist via Spring Data JPA.

Entity example:

```java
@Entity
@Audited
@Table(name = "user_account")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
  private Long id;

  private String username;
  private String firstName;
  private String lastName;
}
```

Service operation example:

```java
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public UserEntity createUser() {
    UserEntity user = new UserEntity();
    user.setUsername("czetsuya");
    user.setFirstName("Edward");
    user.setLastName("Legaspi");
    return userRepository.save(user); // audit row is created automatically
  }
}
```

## 7. Full CRUD and revision endpoint example

This repository demonstrates a flow where the app exposes operational APIs plus revision retrieval endpoints.

### CRUD endpoints

- `POST /users` -> create user
- `PUT /users/{userId}` -> update user
- `DELETE /users/{userId}` -> delete user
- `GET /users/{userId}` -> get current user

### Revision endpoints

- `GET /nerv-audit/com.czetsuyatech.envers.persistence.entity.UserEntity?offset=2&limit=2`

Controller location:
- `src/main/java/com/czetsuyatech/nerv/envers/web/controller/UserController.java`
- `src/main/java/com.czetsuyatech.audit.web.controller/AuditController.java`

Service revision query implementation uses Envers `AuditReader`:

```java
AuditReader auditReader = AuditReaderFactory.get(entityManager);

List<Object[]> revisions = auditReader.createQuery()
    .forRevisionsOfEntity(UserEntity.class, false, true)
    .getResultList();
```

## 8. Run the app

```bash
mvn spring-boot:run
```

## 9. Try the flow (API examples)

### 9.1 Create

```bash
curl -X POST http://localhost:8080/users
```

### 9.2 Update

```bash
curl -X PUT http://localhost:8080/users/1
```

### 9.3 Delete

```bash
curl -X DELETE http://localhost:8080/users/1
```

### 9.4 Read current record

```bash
curl http://localhost:8080/users/1
```

### 9.5 Read revisions

```bash
curl http://localhost:8080/nerv-audit/horizontal/UserEntity?offset=2&limit=10
curl http://localhost:8080/nerv-audit/vertical/entity.persistence.com.czetsuyatech.nerv.envers.UserEntity?offset=2&limit=2
```

## 10. Expected behavior

- Create produces revision type `ADD=0`.
- Update produces revision type `MOD=1`.
- Delete produces revision type `DEL=2`.
- Returned revision payload contains entity snapshot and revision metadata (revision number, timestamp, type).

## 11. Notes for strategy selection

- If `nerv.audit.audit-strategy-type: horizontal`, use horizontal changelog and tables.
- If `nerv.audit.audit-strategy-type: vertical`, use vertical changelog and tables.
- Keep the selected strategy and Liquibase schema in sync before startup.

## 12. Tests

Run the demo test suite with:

```bash
mvn test
```

The suite covers the user REST controller, service behavior, and an H2-backed Spring Boot
integration scenario that starts NERV Audit and persists a user through the demo service. The
integration test uses horizontal auditing because the production vertical strategy requires the
corresponding Liquibase audit-table schema.

## 13. Project references

- App entry: `src/main/java/com/czetsuyatech/nerv/envers/Application.java`
- Entity: `src/main/java/com/czetsuyatech/nerv/envers/persistence/entity/UserEntity.java`
- Service: `src/main/java/com/czetsuyatech/nerv/envers/service/impl/UserServiceImpl.java`
- Controller: `src/main/java/com/czetsuyatech/nerv/envers/web/controller/UserController.java`
- Config: `src/main/resources/application.yml`

