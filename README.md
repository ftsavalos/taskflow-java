# TaskFlow — Java 17 API demo

A compact task workflow API built with Java 17 and the JDK HTTP server. Designed as a small, readable portfolio sample that runs without downloading dependencies.

## What it demonstrates

- Immutable records and an exhaustive switch expression
- Domain logic separated from the HTTP adapter
- Validated input and explicit TODO → IN_PROGRESS → DONE transitions
- Atomic mutations and stable, immutable list snapshots
- JSON responses with 400, 404, 405 and 409 error handling
- Domain tests including concurrent creation, plus GitHub Actions CI

## Run

Requires JDK 17 or later. Commands work in Bash, Git Bash and compatible shells.

```sh
mkdir -p out
javac -d out src/main/java/dev/portfolio/taskflow/*.java src/test/java/dev/portfolio/taskflow/*.java
java -ea -cp out dev.portfolio.taskflow.TaskServiceTest
java -cp out dev.portfolio.taskflow.Application
```

Pass a port as the final argument to use a different port, e.g. `Application 9090`.

## Try it

```sh
curl -X POST 'http://localhost:8080/tasks?title=Learn%20Java'
# {"id":1,"title":"Learn Java","status":"TODO"}

curl http://localhost:8080/tasks
curl -X POST http://localhost:8080/tasks/1/advance
# {"id":1,"title":"Learn Java","status":"IN_PROGRESS"}

curl -X POST http://localhost:8080/tasks/1/advance
# {"id":1,"title":"Learn Java","status":"DONE"}

curl -i -X POST http://localhost:8080/tasks/1/advance
# HTTP 409: completed tasks cannot advance
```

| Method | Route | Result |
| --- | --- | --- |
| GET | `/tasks` | List tasks in creation order |
| POST | `/tasks?title=...` | Create a task; title must be 1–120 characters after trimming |
| POST | `/tasks/{id}/advance` | Advance one workflow step |

## Design and scope

`TaskService` owns validation, IDs and transition rules. `Application` translates HTTP requests into domain calls. A synchronized service is sufficient for this bounded demo and avoids races between read and update. The server uses four worker threads and binds to loopback.

This is an educational demo: data is lost on restart, and there is no authentication, pagination or persistent database. Creation deliberately uses a query parameter to keep the example dependency-free. A larger version would use JSON request bodies with a JSON library, persistent storage, structured logging and integration tests. The Location header identifies the new task; this minimal version only exposes collection listing, not individual-task retrieval.

## CI

The workflow compiles the sources with JDK 17 and runs assertions-enabled domain tests on pushes and pull requests. Local verification was unavailable in the preparation environment because it lacked javac. Run the commands above and check CI before relying on the demo.

## Suggested extensions

1. Add Spring Boot and JSON request DTOs.
2. Implement persistence with PostgreSQL and optimistic locking.
3. Add JUnit API integration tests and OpenAPI documentation.
