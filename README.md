# Java Spring Boot Starter Template 🚀

Production-ready Spring Boot 3.5.x template featuring JWT-based authentication, PostgreSQL integration, OpenAPI, WebSocket support, and Feign clients. Designed for rapid bootstrapping with clean structure and pragmatic defaults.

## Key Features

- Authentication: JWT access tokens, `AuthController` with `/login` and `/register`
- Authorization: Role-based via `@PreAuthorize` and Spring Security
- Persistence: Spring Data JPA with PostgreSQL and JPA Auditing
- API Docs: OpenAPI 3 via springdoc (`/swagger-ui`)
- WebSocket: STOMP endpoint at `/ws` with simple broker `/topic`
- HTTP Clients: Spring Cloud OpenFeign + OkHttp (Gemini API integration)
- Observability-friendly: Centralized exception handling and consistent API response envelope
- Config: `.env` support with `spring-dotenv`

## Tech Stack

| Layer | Tech |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Modules | Web, Security, Validation, Data JPA, WebSocket, Retry, Cache |
| API Docs | springdoc-openapi-starter-webmvc-ui |
| Auth | JJWT (io.jsonwebtoken) |
| DB | PostgreSQL |
| Client | Spring Cloud OpenFeign + OkHttp |
| Build | Maven |

Notes:
- Flyway is not configured in this project. See docs/DEPLOYMENT.md for optional setup steps.
- Actuator is not included by default.

## Folder Structure

```
src/
  main/
    java/com/github/jutionck/
      config/            # Security, OpenAPI, Feign, WebSocket, Cache, Async
      controller/        # REST controllers (Auth, User)
      dto/               # Request/Response DTOs
      entity/            # JPA entities (User, BaseEntity)
      enums/             # Enums (UserRole)
      exceptions/        # GlobalExceptionHandler + custom exceptions
      repository/        # Spring Data repositories
      security/          # JWT provider, filter, handlers
      service/           # Business services (AuthService, UserService)
      client/            # Feign clients (Gemini)
      seeder/            # AdminSeeder (bootstrap admin)
      utils/             # ResponseUtil
    resources/
      application.properties
  test/
```

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL 14+

### Configuration
Set environment variables (recommended: `.env` in project root). See `.env.example` for full list.

Example `.env` (do not commit secrets):
```
SERVER_PORT=9999
DB_HOST=localhost
DB_PORT=5432
DB_NAME=app_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace-with-long-random-256-bit-secret
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=1209600000
JWT_ISSUER=starter-template
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=ChangeMe123!
GEMINI_API_URL=https://generativelanguage.googleapis.com
GEMINI_API_KEY=your-gemini-api-key
```

The app reads from `.env` via `spring-dotenv` and `application.properties`.

### Run Locally (Maven)
```
# Start PostgreSQL locally first
mvn spring-boot:run
```

Build a JAR and run:
```
mvn clean package
java -jar target/java-spring-boot-starter-template-0.0.1-SNAPSHOT.jar
```

### Optional: Run with Docker
This repo doesn’t include a `Dockerfile` or Compose by default. Use the sample below.

Sample `Dockerfile`:
```
FROM eclipse-temurin:21-jre as runtime
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9999
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Sample `docker-compose.yml`:
```
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: app_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
  app:
    build: .
    depends_on:
      - db
    env_file: .env
    ports:
      - "9999:9999"
```

Commands:
```
docker compose up -d --build
docker compose logs -f app
```

## API Documentation

- Swagger UI: http://localhost:9999/swagger-ui/index.html
- OpenAPI JSON: http://localhost:9999/v3/api-docs

Actuator endpoints are not enabled by default. Add `spring-boot-starter-actuator` to enable.

## Example Endpoints

### Auth – Login
Request:
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin@example.com",
  "password": "admin@example.com"
}
```

Response (201):
```
{
  "status": { "code": 201, "description": "Created" },
  "data": {
    "accessToken": "<JWT>",
    "tokenType": "Bearer",
    "role": "ROLE_ADMIN"
  }
}
```

### Users – Me (requires JWT)
```
GET /api/v1/users/me
Authorization: Bearer <JWT>
```

Sample response:
```
{
  "username": "admin@example.com",
  "authorities": [ { "authority": "ROLE_ADMIN" } ]
}
```

### Error Response (standardized)
```
{
  "status": { "code": 401, "description": "Unauthorized - Invalid or missing token" },
  "errors": ["You must provide a valid access token"]
}
```

## Common Commands

```
mvn test                      # Run tests
mvn package                   # Build JAR
mvn spring-boot:run           # Run dev server
docker compose up -d          # Start Docker (if using sample compose)
```

## Useful Links

- Architecture: docs/ARCHITECTURE.md
- Security: docs/SECURITY.md
- Deployment: docs/DEPLOYMENT.md
- Contributing: docs/CONTRIBUTING.md

## License & Credits

- License: MIT — see LICENSE
- Credits: Built by Jution Candra Kirana and contributors.
