# Architecture 🧩

This project follows a clean, modular Spring Boot architecture with clear separation of concerns across layers.

## Layers Overview

- Domain Layer
  - `entity/`: JPA entities (e.g., `User`, `BaseEntity` with auditing)
  - `enums/`: Domain enums (`UserRole`)
- Application Layer
  - `controller/`: HTTP endpoints (`AuthController`, `UserController`)
  - `service/`: Business logic (`AuthService`, `UserService`)
  - `dto/`: API request/response contracts
- Infrastructure Layer
  - `repository/`: Data access (`UserRepository`)
  - `security/`: JWT token provider, filter, handlers
  - `config/`: Security, OpenAPI, Feign, WebSocket, Cache, Async
  - `client/`: External API clients (Gemini via OpenFeign)
- Cross‑Cutting
  - `exceptions/`: Global exception handling and custom exceptions
  - `utils/`: Helpers (`ResponseUtil`)

## Responsibilities

- Controller: Validate input, orchestrate service calls, map to API responses
- Service: Encapsulate use cases and business rules
- Repository: Data persistence with Spring Data JPA
- Security: Authentication/authorization, token parsing, and error handling
- Config: Framework and infrastructure configuration (beans, filters, docs)

## Data Flow

1. Client calls REST endpoint (e.g., `POST /api/v1/auth/login`).
2. Security filter extracts and validates JWT (if present).
3. Controller delegates to Service.
4. Service uses Repository for persistence or Client for outbound calls.
5. Response is wrapped with `ApiResponse` via `ResponseUtil`.

## Mermaid (High-Level)

```mermaid
flowchart LR
  C[Client] -->|HTTP| Ctl[Controller]
  Ctl -->|Validate| Svc[Service]
  subgraph Security
    F[JwtAuthenticationFilter]
    TP[JwtTokenProvider]
  end
  C -.->|Bearer JWT| F
  F --> TP
  Svc --> Repo[Repository]
  Svc --> Ext[Feign Client (Gemini)]
  Repo --> DB[(PostgreSQL)]
  Ext --> API[(Gemini API)]
  Svc --> Resp[ResponseUtil]
  Resp --> C
```

## Design Principles

- Clean Architecture: Dependency rule (controllers/services depend on domain contracts)
- SOLID: Especially SRP, OCP in services and config
- Defensive coding: Validations with Jakarta Validation, centralized error handling
- Convention over configuration: Spring Boot defaults where appropriate

## Notable Components

- `SecurityConfig`: Stateless JWT security, `permitAll` for auth and Swagger
- `JwtAuthenticationFilter`: Extracts, validates token, populates `SecurityContext`
- `JwtTokenProvider`: Generates/verifies JWT with role claim
- `OpenAPIConfig`: Swagger + JWT bearer scheme
- `WebSocketConfig`: STOMP endpoint `/ws`, app prefix `/app`, broker `/topic`
- `GeminiFeignClient` + `GeminiFeignConfig`: Feign + OkHttp with API key header

## Extensibility

- Add new features by creating DTOs, controllers, services, repositories within the same structure.
- Introduce modules or packages per bounded context if needed (e.g., `feature/<context>` package with the same internal layout).
