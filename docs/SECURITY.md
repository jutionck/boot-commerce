# Security 🔐

This project implements stateless JWT authentication and role-based authorization using Spring Security.

## Overview

- Authentication: `AuthService` authenticates credentials and issues JWTs via `JwtTokenProvider`.
- Authorization: Endpoints require authenticated requests unless explicitly permitted. Method-level rules via `@PreAuthorize`.
- Filters: `JwtAuthenticationFilter` extracts and validates `Authorization: Bearer <token>` for each request.
- Handlers: `JwtAuthenticationHandler` provides consistent 401/403 JSON error bodies.

## Security Configuration

`SecurityConfig`:
- CSRF disabled (stateless APIs)
- Session policy: `STATELESS`
- Permit: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- All other routes require authentication
- Registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

## JWT Token

`JwtTokenProvider` builds tokens with:
- Subject: `username`
- Issuer: from `JWT_ISSUER`
- Expiration: now + `JWT_EXPIRATION` (ms)
- Claims: `role` (e.g., `ROLE_ADMIN`)
- Signature: HMAC with secret `JWT_SECRET`

Example payload (decoded):
```
{
  "sub": "admin@example.com",
  "iss": "starter-template",
  "exp": 1735689600,
  "iat": 1735603200,
  "role": "ROLE_ADMIN"
}
```

Validation path:
1. `JwtAuthenticationFilter` checks `Authorization` header and extracts token.
2. `JwtTokenProvider.verifyToken(...)` parses and verifies signature and expiration.
3. On success, `UserService.loadUserByUsername(...)` loads the user and populates `SecurityContext`.
4. On failure, `JwtAuthenticationException` triggers 401 with a standard error body.

## Authorization

- Roles are provided by `UserRole` enum and exposed as `ROLE_*` authorities (defined roles: `USER`, `ADMIN`).
- Example usage: `@PreAuthorize("hasRole('ADMIN')")` to restrict to admins.
- Note: `UserController` currently uses `@PreAuthorize("hasRole('RESIDENCE')")`. Since `RESIDENCE` is not in `UserRole`, this will always deny access; adjust to a defined role.

## Endpoints Summary

- Public: `POST /api/v1/auth/login`, `POST /api/v1/auth/register`, Swagger paths
- Protected: Everything else (e.g., `GET /api/v1/users/me`)

## Token Expiration and Refresh

- Access token expiration: `JWT_EXPIRATION` (milliseconds) from environment.
- Refresh token settings: `JWT_REFRESH_EXPIRATION` is defined in configuration but refresh endpoint/flow is not implemented.
  - To add refresh: implement an endpoint that verifies a long-lived refresh token and issues a new access token.

## Error Responses

Standardized responses created by `JwtAuthenticationHandler`/`ResponseUtil`:
- 401 Unauthorized
```
{
  "status": { "code": 401, "description": "Unauthorized - Invalid or missing token" },
  "errors": ["You must provide a valid access token"]
}
```
- 403 Forbidden
```
{
  "status": { "code": 403, "description": "Forbidden - Access denied" },
  "errors": ["You do not have permission to access this resource"]
}
```

## Password Storage

- `BCryptPasswordEncoder` is used to hash passwords.
- `AdminSeeder` creates an initial admin based on `ADMIN_USERNAME` and `ADMIN_PASSWORD` when not present.

## Hardening Tips

- Ensure `JWT_SECRET` is a strong 256-bit random value.
- Prefer `https` in production; never transmit tokens over insecure channels.
- Set reasonable token TTLs and consider rolling keys.
- Add account lockout and login attempt rate limiting where applicable.
- Optionally enable Spring Security’s CORS, CSP headers via a security filter.
