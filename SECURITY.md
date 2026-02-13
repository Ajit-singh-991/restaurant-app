# Security Checklist

This document summarizes security-related configuration and recommendations for the Restaurant Management System.

## Authentication & Secrets

- **JWT**: Secret is read from `JWT_SECRET` (default in dev only). **Production**: set a strong `JWT_SECRET` (e.g. 256-bit) and never commit it.
- **Passwords**: BCrypt encoding; seed users use a known dev password (`password123`). **Production**: change all default passwords and avoid default credentials.
- **Token expiry**: Configurable via `JWT_EXPIRATION_MS` (default 24h).

## API & Network

- **HTTPS**: Use TLS in production; configure reverse proxy (e.g. Nginx) to terminate SSL.
- **CORS**: Allowed origins are set via `CORS_ORIGINS`. Restrict to your frontend origins in production.
- **Rate limiting**: In-memory rate limit is enabled by default (`app.rate-limit.enabled: true`, `app.rate-limit.requests-per-minute: 120`). Configure with `RATE_LIMIT_ENABLED` and `RATE_LIMIT_REQUESTS_PER_MINUTE`. For multi-instance deployments, consider a shared store (e.g. Redis).

## Application Config

- **Database**: Use strong credentials via `POSTGRES_USER` / `POSTGRES_PASSWORD`; do not use defaults in production.
- **File upload**: Menu image upload path is configurable (`UPLOAD_MENU_DIR`); validate and restrict file types (done in `FileUploadController`).

## Monitoring & Logging

- **Logback**: Logging is configured in `application.yml`; adjust levels per environment.
- **Optional**: Frontend error reporting (e.g. Sentry) and uptime monitoring can be added for production.

## Verification

- [ ] Default passwords changed in production
- [ ] `JWT_SECRET` set via environment in production
- [ ] CORS restricted to known origins
- [ ] HTTPS enabled at reverse proxy
- [ ] Rate limiting enabled and tuned for load
