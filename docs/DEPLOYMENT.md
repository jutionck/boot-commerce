# Deployment 🚀

This guide covers local development, optional Dockerization, and production deployment options.

## Local Development (Maven)

1. Ensure PostgreSQL is running and accessible.
2. Copy `.env.example` to `.env` and set values.
3. Start the app:
```
mvn spring-boot:run
```

Build a JAR for local/prod:
```
mvn clean package
java -jar target/java-spring-boot-starter-template-0.0.1-SNAPSHOT.jar
```

## Optional: Docker & Compose

This repository does not include Docker files by default. Use the following examples if you want containerized dev/prod:

`Dockerfile` (multi-stage build recommended for CI/CD):
```
# Build stage
FROM eclipse-temurin:21-jdk as build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

`docker-compose.yml`:
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

## Environments & Profiles

- Current configuration uses a single `application.properties` and `.env` overrides.
- To add profiles, create `application-local.properties`, `application-dev.properties`, `application-prod.properties` and run with `-Dspring.profiles.active=prod`.

## Database Migrations (Optional Flyway)

Flyway is not configured in this project. To add:
1. Add dependency in `pom.xml`:
```
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```
2. Create migration files under `src/main/resources/db/migration` (e.g., `V1__init.sql`).
3. Configure connection via existing datasource environment variables.

## CI/CD (GitHub Actions example)

Recommended workflow steps:
- Checkout, set up JDK 21
- Cache Maven
- Build and test (`mvn -B -DskipTests=false verify`)
- Build Docker image and push to registry (if applicable)
- Deploy to environment (compose, k8s, or PaaS)

## Production Notes

- Provide secure environment variables at deploy time (never commit secrets).
- Use managed Postgres or hardened self-hosted instance with backups.
- Run behind an HTTPS reverse proxy (e.g., Nginx, API Gateway) and enable TLS.
- Monitor logs, metrics, and health checks (consider adding Spring Actuator).
