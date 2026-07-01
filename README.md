# NERV Examples

A collection of production-ready example applications demonstrating how to use the **NERV** ecosystem in real-world Spring Boot projects.

Rather than providing isolated code snippets, this repository contains complete, runnable applications that showcase recommended architectures, best practices, and common integration patterns for NERV libraries.

---

## Why This Repository?

Documentation explains **what** a library does.

Examples demonstrate **how** to use it correctly.

Each project is designed to be:

- ✅ Complete and runnable
- ✅ Production-oriented
- ✅ Focused on a specific library or feature
- ✅ Easy to understand and extend

Whether you're evaluating a NERV library or integrating it into an existing application, these examples provide a practical starting point.

---

## Available Examples

| Project | Description |
|---------|-------------|
| **nerv-audit-spring-boot-audit-trail-demo** | Demonstrates how to integrate **nerv-audit** into a Spring Boot application to provide entity auditing, revision history, and audit trails. |
| **nerv-exception-spring-boot-demo** | Demonstrates standardized exception handling, validation, and RFC 7807 Problem Details responses using **nerv-exception**. |

---

## Repository Structure

```text
nerv-examples
├── nerv-audit-spring-boot-audit-trail-demo
└── nerv-exception-spring-boot-demo
```

Additional examples will be added as the NERV ecosystem grows.

Planned examples include:

- nerv-identity
- nerv-events
- nerv-cache
- nerv-resilience
- nerv-trade

---

## Requirements

- Java 21+
- Maven 3.9+
- Spring Boot 3.x

Each example may include additional requirements. Refer to the individual project's README for project-specific instructions.

---

## Running an Example

Clone the repository:

```bash
git clone https://github.com/czetsuyatech/nerv-examples.git
```

Navigate to an example:

```bash
cd nerv-examples/nerv-exception-spring-boot-demo
```

Run the application:

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

## Design Philosophy

Every example follows the same principles that guide the NERV libraries:

- Production-ready architecture
- Clean and maintainable code
- Modern Spring Boot practices
- Minimal boilerplate
- Clear separation of concerns
- Opinionated best practices

The goal is to provide examples that can serve as a foundation for real applications—not just simple demos.

---

## Related Projects

- **nerv-audit** – Advanced auditing and entity history for Spring Boot
- **nerv-exception** – Standardized exception handling with RFC 7807 Problem Details
- **nerv-actions** – Reusable GitHub Actions for Java and Maven projects

---

## Contributing

Contributions are welcome.

If you'd like to contribute an example or improve an existing one, feel free to open an issue or submit a pull request.

---

## License

This repository is licensed under the MIT License.
