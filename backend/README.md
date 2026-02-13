# Restaurant Management - Backend

Spring Boot **3.2.1** REST API with JWT authentication, WebSocket (STOMP), and PostgreSQL. Single Maven module; package: `com.restaurant`.

## Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **PostgreSQL 15+**

## Setup

### Database

```bash
# Option 1: Docker (from repo root)
docker-compose up -d postgres

# Option 2: Local PostgreSQL
createdb restaurant_db
```

### Run

```bash
# Development
mvn spring-boot:run

# With profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/restaurant-management-1.0.0-SNAPSHOT.jar
```

API base: **http://localhost:8080/api**

### Tests

```bash
mvn test
```

Uses H2 in-memory and test profile. Single test class:

```bash
mvn test -Dtest=OrderServiceTest
```

## Configuration

Config: `src/main/resources/application.yml`. Override with environment variables.

| Variable | Default | Description |
|----------|---------|-------------|
| POSTGRES_HOST | localhost | DB host |
| POSTGRES_PORT | 5432 | DB port |
| POSTGRES_DB | restaurant_db | DB name |
| POSTGRES_USER | restaurant_admin | DB user |
| POSTGRES_PASSWORD | changeme | DB password |
| SERVER_PORT | 8080 | Server port |
| JWT_SECRET | (dev default) | JWT key; change in production |
| JWT_EXPIRATION_MS | 86400000 | Token expiry (24h) |
| CORS_ORIGINS | localhost:4200-4203 | Allowed origins |
| RATE_LIMIT_ENABLED | true | Rate limiting |
| MAIL_RECEIPT_ENABLED | false | Email receipts |
| MAIL_FROM | noreply@restaurant.com | Sender for receipts |
| RESET_SEED_PASSWORDS | false | If true, on startup all user passwords are reset to `password123`. Use once if login fails, then set back to false. |

Context path is **/api**; all endpoints live under `http://localhost:8080/api`.

**If login fails with seed users (e.g. admin / password123):** set `RESET_SEED_PASSWORDS=true`, start the backend once (it resets all passwords to `password123`), then set the flag back to false and restart.

## Database Migrations (Flyway)

Migrations in `src/main/resources/db/migration/`:

- **V1** - initial_schema.sql (users, categories, menu_items, tables, orders, payments, etc.)
- **V2** - seed_data.sql (users, categories, items, tables)
- **V3** - reviews_table.sql
- **V4** - loyalty_and_staff_tables.sql
- **V5** - delivery_and_campaigns.sql
- **V6** - invoices_table.sql
- **V7** - menu_allergens_and_kitchen_stations.sql
- **V8** - category_image_url.sql

Hibernate `ddl-auto` is **validate**; schema is managed only by Flyway.

## Project Structure

```
src/main/java/com/restaurant/
├── config/       SecurityConfig, WebSocketConfig, CorsConfig, RateLimitFilter
├── controller/   Auth, Menu, Order, Kitchen, Table, Reservation, Payment,
│                 Analytics, AdvancedAnalytics, Review, Loyalty, Staff, QR,
│                 Delivery, Campaign, Recommendation
├── dto/          Request/response DTOs
├── entity/       User, Category, MenuItem, KitchenStation, Order, OrderItem, etc.
├── repository/   JPA repositories
├── service/      Business logic (Auth, Menu, Order, Kitchen, Payment, etc.)
├── security/     JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal
└── exception/    GlobalExceptionHandler
```

## API Summary

- **Auth** - POST login/register, GET me, refresh; JWT in Authorization header.
- **Menu** - GET categories/items (public); ADMIN: CRUD, bulk upload, image upload.
- **Orders** - Create, list, status, cancel, split (equal/percentage/custom).
- **Kitchen** - Active orders, by station, start/ready, item complete, stats, stations.
- **Tables, Reservations, Payments, Analytics, Reviews, Loyalty, Staff, QR, Delivery, Campaigns, Recommendations** - see root CLAUDE.md.

## WebSocket

STOMP over SockJS at **ws://localhost:8080/api/ws**. Topics: `/topic/kitchen`, `/topic/orders`, `/topic/waiter`, `/topic/tables`.

## Production Build

```bash
mvn clean package -DskipTests
```

JAR: `target/restaurant-management-1.0.0-SNAPSHOT.jar`. Set JWT_SECRET and DB credentials via env.
