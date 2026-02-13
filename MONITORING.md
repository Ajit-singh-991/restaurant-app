# Monitoring and Optional Integrations

## Logging

- **Backend**: Logback is configured in `application.yml`. Adjust `logging.level.com.restaurant` and `logging.level.org.springframework.security` per environment.
- **Frontend**: Browser console; consider a centralised logger in production.

## Scheduled Reports (Optional)

The Reports page (`/dashboard/reports`) allows generating and downloading reports on demand. **Scheduled reports** (e.g. daily/weekly email) are not implemented. To add them:

1. **Backend**: Use Spring `@Scheduled` (e.g. `@Scheduled(cron = "0 0 8 * * *")` for 8 AM daily) in a service that:
   - Calls `AnalyticsService.exportReport()` for the desired type/format/date range.
   - Sends the result by email (Spring Mail) to configured recipients.
2. **Configuration**: Add `app.reports.schedule.enabled`, `app.reports.recipients`, and cron expression to `application.yml`; use a dedicated SMTP config for report delivery.

## Sentry (Optional)

For error tracking in production:

1. **Frontend**: Add `@sentry/angular` (or `@sentry/browser`), initialise in `main.ts` with your DSN, and set `environment.production` so Sentry is only enabled in prod.
2. **Backend**: Add `sentry-spring-boot-starter`, set `SENTRY_DSN` and optionally `SENTRY_ENVIRONMENT`; Sentry will capture unhandled exceptions and log levels you configure.

## Uptime and Alerts

- Use an external uptime checker (e.g. UptimeRobot, Pingdom) to hit a health endpoint.
- Spring Boot Actuator can expose `/actuator/health`; add `spring-boot-starter-actuator` and secure the endpoint if you use it.
