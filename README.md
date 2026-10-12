# TaskFlow — Spring Boot task API

A compact Java 17 portfolio project using Spring Boot 3.5, Spring MVC, Jackson and Jakarta Bean Validation. It models a simple TODO → IN_PROGRESS → DONE workflow.

[![Java CI](https://github.com/ftsavalos/taskflow-java/actions/workflows/ci.yml/badge.svg)](https://github.com/ftsavalos/taskflow-java/actions/workflows/ci.yml)

## Run

Install JDK 17+ and Maven 3.6.3+, then:

```sh
mvn verify
mvn spring-boot:run
```

Or run the packaged executable JAR:

```sh
mvn package
java -jar target/taskflow-java-1.0.0.jar
```

The API listens on `http://localhost:8080`. Override the port with `java -jar target/taskflow-java-1.0.0.jar --server.port=9090`.

## Try it

```sh
curl -i -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Learn Spring Boot"}'
# HTTP 201, Location: /tasks/1
# {"id":1,"title":"Learn Spring Boot","status":"TODO"}

curl http://localhost:8080/tasks
curl http://localhost:8080/tasks/1
curl -X POST http://localhost:8080/tasks/1/advance
# {"id":1,"title":"Learn Spring Boot","status":"IN_PROGRESS"}

curl -X POST http://localhost:8080/tasks/1/advance
# {"id":1,"title":"Learn Spring Boot","status":"DONE"}

curl -i -X POST http://localhost:8080/tasks/1/advance
# HTTP 409 with a problem-detail JSON response
```

| Method | Route | Result |
| --- | --- | --- |
| GET | `/tasks` | List tasks in creation order |
| GET | `/tasks/{id}` | Read one task; 404 if absent |
| POST | `/tasks` | Create from JSON `{ "title": "..." }`; 201 |
| POST | `/tasks/{id}/advance` | Advance one step; 409 after DONE |

Creation requires an `application/json` body. The title is trimmed before validation and must contain 1–120 characters. Missing, null, blank or oversized titles and malformed JSON return 400. The earlier query-parameter creation format is replaced by this JSON contract.

## Design

- `Application`: Spring Boot entry point and domain-service bean.
- `TaskController`: HTTP mapping with constructor injection.
- `CreateTaskRequest`: immutable request DTO with `@NotBlank` and `@Size`.
- `TaskResponse`: response DTO separating the HTTP contract from the domain.
- `TaskService`: framework-independent domain rules, immutable records, synchronized mutations and immutable list snapshots.
- `ApiExceptionHandler`: problem-detail responses for validation, missing tasks and invalid transitions. Spring handles unsupported methods and media types.

Jackson handles JSON serialization and deserialization. Request DTOs expose only editable fields: clients cannot assign IDs or initial statuses.

## Tests and CI

`mvn verify` runs JUnit domain tests (including concurrent creation) and Spring Boot/MockMvc HTTP integration tests covering creation, lookup, listing, transitions, validation, malformed JSON, Unicode, and HTTP error codes. GitHub Actions runs the same command and packages the executable JAR.

## Scope

Tasks are stored in memory and lost on restart. There is no authentication or pagination; the default server binds to loopback for local demonstrations. This is a small educational API, not a production service.

Possible next steps: PostgreSQL persistence with optimistic locking, OpenAPI documentation, and pagination.
