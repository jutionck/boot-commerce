# Contributing 🤝

Thanks for your interest in contributing! This guide explains the workflow and conventions.

## Branch Strategy

- Mainline: `main` (protected)
- Branches: `feature/<short-desc>`, `fix/<short-desc>`, `chore/<short-desc>`

## Commit Messages

Use Conventional Commits:
- `feat: add user search endpoint`
- `fix: correct jwt expiration unit`
- `chore: bump spring-boot to 3.5.7`
- `docs: add architecture diagram`

## Pull Requests

- Keep PRs focused and small.
- Describe the change, rationale, and testing notes.
- Link related issues.
- Request at least one review before merge.

## Testing & Quality

- Run tests locally: `mvn test`
- CI should run `mvn -B -DskipTests=false verify` before merge.
- Linting/style (recommended):
  - Spotless (formatting) and Checkstyle (static checks). Not configured by default—consider adding Maven plugins.

## Adding New Features

1. Model the domain: new `entity`, `enum` if needed.
2. Define DTOs for requests/responses.
3. Implement `service` logic and `repository` queries.
4. Expose endpoints in `controller` and annotate for OpenAPI.
5. Secure endpoints with roles via `@PreAuthorize` as appropriate.
6. Add tests and update docs.

## API Documentation

- Add/adjust annotations to keep Swagger accurate.
- Ensure new endpoints conform to the `ApiResponse<T>` envelope for consistency.

## Coding Guidelines

- Prefer constructor injection (`@RequiredArgsConstructor`) for components.
- Keep controllers thin; push logic to services.
- Handle errors via exceptions and the global handler.
- Keep package structure consistent by feature or layer.
