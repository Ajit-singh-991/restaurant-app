# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture

Monorepo with two top-level directories:

- **`backend/`** — Spring Boot 3.2.1 (Java 17, Maven). Package: `com.restaurant`.
- **`frontend/`** — Angular 17 workspace with 4 apps + 1 shared library.

### Frontend Apps

| App | Port | Default route | Allowed roles |
|-----|------|---------------|---------------|
| customer-app | 4200 | `/menu` | Public (orders require login) |
| waiter-app | 4201 | `/tables` | WAITER, ADMIN |
| kitchen-app | 4202 | `/` (KitchenDisplay) | KITCHEN, ADMIN |
| admin-app | 4203 | `/dashboard` | ADMIN, MANAGER |

### Shared Library

Path alias: `@shared` (maps to `projects/shared/src/public-api.ts`), `@shared/*` (maps to `projects/shared/src/lib/*`).

Contains: models, services (auth, cart, menu, order, table, analytics, payment, websocket, kitchen, review, loyalty, staff, qr-code, delivery, advanced-analytics, campaign, recommendation), guards (auth, role), interceptors (jwt, error), SharedModule, LoginPageComponent, and LanguageSelectorComponent.

All exports go through `public-api.ts` — new services/models must be added there.

All apps use NgModules (non-standalone) with lazy-loaded feature modules and SCSS styling.

Each app has its own `environment.ts` with `apiUrl` and `wsUrl`. The `@environments` alias resolves per-project via `tsconfig.json` path mappings.

## Commands

### Backend (run from `backend/`)

```bash
mvn spring-boot:run                   # Start dev server (port 8080)
mvn clean package                     # Build JAR
mvn test                              # Run all tests (uses H2 in-memory DB)
mvn test -Dtest=OrderServiceTest      # Run a single test class
mvn clean package -DskipTests         # Build without tests
```

### Frontend (run from `frontend/`)

```bash
npm install                           # Install dependencies
npm run start:customer                # Serve customer-app on :4200
npm run start:waiter                  # Serve waiter-app on :4201
npm run start:kitchen                 # Serve kitchen-app on :4202
npm run start:admin                   # Serve admin-app on :4203
npm run build                         # Production build all apps
ng build kitchen-app                  # Build single app
ng test                               # Run all Karma/Jasmine tests
ng test kitchen-app                   # Run tests for a single app
```

### Docker

```bash
docker-compose up -d                  # Start all services (postgres, backend, frontend)
docker-compose down                   # Stop all services
docker-compose up --build             # Rebuild images and start
```

## API

- **Context path:** `/api` — all endpoints are prefixed with `/api`
- **Base URL:** `http://localhost:8080/api`
- **WebSocket:** `ws://localhost:8080/api/ws` (STOMP over SockJS)

### Key Endpoints

| Prefix | Controller | Access |
|--------|-----------|--------|
| `/auth` | AuthController | Public: login, register. Auth: me, refresh, change-password |
| `/menu` | MenuController | Public: GET. ADMIN: POST/PUT/DELETE |
| `/orders` | OrderController | Authenticated |
| `/kitchen` | KitchenController | KITCHEN or ADMIN (includes per-item status, stats) |
| `/tables` | TableController | Authenticated (ADMIN to create/delete) |
| `/reservations` | ReservationController | Auth: CRUD. WAITER/ADMIN/MANAGER: status updates |
| `/payments` | PaymentController | Auth: process, view. ADMIN/MANAGER: list, refund, stats |
| `/analytics` | AnalyticsController | ADMIN/MANAGER: dashboard, sales, menu analytics |
| `/reviews` | ReviewController | Public: GET item reviews. Auth: create, my reviews. ADMIN/MANAGER: all, respond, stats |
| `/loyalty` | LoyaltyController | Auth: my account, transactions, redeem. ADMIN/MANAGER: all accounts |
| `/staff` | StaffController | Auth: clock-in/out, session, history. ADMIN/MANAGER: active sessions, summary, tips |
| `/qr` | QrCodeController | ADMIN/MANAGER: generate table QR codes (PNG), get order URLs |
| `/delivery` | DeliveryController | Auth: create, status, my. ADMIN/MANAGER: assign, active, pending, stats |
| `/analytics/advanced` | AdvancedAnalyticsController | ADMIN/MANAGER: forecast, customer segments, menu optimization, peak hours |
| `/campaigns` | CampaignController | Auth: validate/redeem promo. ADMIN/MANAGER: create, list, status, stats |
| `/recommendations` | RecommendationController | Auth: personalized, trending, item pairings |

## Authentication

JWT-based (jjwt 0.12.3). Token in `Authorization: Bearer <token>` header.

- Secret: `app.jwt.secret` (env: `JWT_SECRET`)
- Expiration: 24 hours (86400000 ms)
- Token claims: subject=userId, username, role, iat, exp
- Password encoding: BCrypt

### Roles

`ADMIN`, `MANAGER`, `WAITER`, `KITCHEN`, `CUSTOMER`

Spring Security prefixes with `ROLE_` (e.g., `ROLE_ADMIN`).

## Test Credentials

All seed users have password: **`password123`**

| Username | Role |
|----------|------|
| admin | ADMIN |
| manager1 | MANAGER |
| waiter1 | WAITER |
| waiter2 | WAITER |
| kitchen1 | KITCHEN |
| customer1–customer5 | CUSTOMER |

## Database

- PostgreSQL 15 (dev default: `restaurant_db`, user: `restaurant_admin`)
- **Flyway migrations** in `backend/src/main/resources/db/migration/`:
  - `V1__initial_schema.sql` — tables: users, categories, menu_items, restaurant_tables, reservations, orders, order_items, order_status_history, payments, ingredients, suppliers, customer_addresses
  - `V2__seed_data.sql` — seed data (users, categories, menu items, tables)
  - `V3__reviews_table.sql` — reviews table with ratings and management responses
  - `V4__loyalty_and_staff_tables.sql` — loyalty_accounts, loyalty_transactions, time_entries
  - `V5__delivery_and_campaigns.sql` — delivery_tracking, campaigns
- Hibernate DDL: `validate` (schema managed by Flyway, not Hibernate)
- Test profile (`application-test.yml`): H2 in-memory, Flyway disabled, `create-drop`

## WebSocket

STOMP over SockJS at `/ws`. Broker prefixes: `/topic`, `/queue`. App prefix: `/app`.

Topics: `/topic/kitchen`, `/topic/orders`, `/topic/waiter`, `/topic/tables`.

Used for real-time order updates to kitchen, waiter, and customer apps. `NotificationService` broadcasts via `SimpMessagingTemplate`.

## Key Patterns

- **Shared imports:** `import { AuthService, MenuItem, authGuard } from '@shared'`
- **Lazy loading:** All feature modules are lazy-loaded via `loadChildren`.
- **Angular Material:** indigo-pink (customer), purple-green (kitchen), deeppurple-amber (admin).
- **Backend utilities:** Lombok for boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`). DTO classes use static inner classes (see `AnalyticsDto`, `KitchenDto`).
- **CORS:** Configurable via `CORS_ORIGINS` env var. Defaults to all dev ports (4200–4203) and `http://localhost`.
- **Kitchen item tracking:** `OrderItem` has its own `ItemStatus` enum. `KitchenService.markItemComplete()` auto-marks the parent order as READY when all items are done.
- **QR Codes:** ZXing library generates PNG QR codes for tables. URL pattern: `{frontendUrl}/menu?table={tableNumber}`.
- **Loyalty tiers:** BRONZE → SILVER (500pts) → GOLD (2000pts) → PLATINUM (5000pts). Points earned per dollar with tier multiplier (1x/1.25x/1.5x/2x).
- **i18n:** `@ngx-translate/core` + `@ngx-translate/http-loader`. Translation files in `assets/i18n/{en,hi,es}.json`. LanguageSelectorComponent in SharedModule. Configured in customer-app with `TranslateModule.forRoot()`.
- **Delivery tracking:** Status state machine: PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED. Auto-completes parent order on DELIVERED. Driver performance stats tracked.
- **Campaign/Marketing:** Supports PERCENTAGE_DISCOUNT, FLAT_DISCOUNT, BUY_ONE_GET_ONE, FREE_DELIVERY, HAPPY_HOUR campaign types. Promo code validation with usage limits and expiration.
- **AI Recommendations:** Personalized (collaborative filtering from order history), trending (week-over-week growth), item pairing suggestions (co-occurrence analysis).

## CI/CD

GitHub Actions workflow at `.github/workflows/ci.yml` with 3 jobs:
- **backend** — Java 17 + Maven + Postgres service container; runs `mvn compile`, `mvn test`, `mvn package`
- **frontend** — Node 18 + `npm ci` + `npm run build` (all apps)
- **docker** — `docker-compose build` (only on `main` branch, after backend+frontend pass)

## Testing

Backend: 11 test classes (JUnit 5 + Mockito) covering Auth, Order, Menu, Payment, Kitchen, Table, Reservation, Analytics, Review, Loyalty, Staff services (143 tests). Run with `mvn test -f backend/pom.xml`.

Frontend: 4 shared service specs (auth, kitchen, order, menu) + 4 app component specs (customer, waiter, kitchen, admin) using Jasmine/Karma + `HttpClientTestingModule`. Run with `cd frontend && ng test`.

## Docker Deployment

Frontend Nginx serves all 4 apps on port 80: `/` (customer), `/waiter`, `/kitchen`, `/admin`. API and WebSocket requests are proxied to backend.

## Entity Enums

- **Order status:** PENDING, CONFIRMED, PREPARING, READY, SERVED, COMPLETED, CANCELLED
- **Order type:** DINE_IN, TAKEAWAY, DELIVERY
- **Item status:** PENDING, PREPARING, READY, SERVED, CANCELLED
- **Table status:** AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
- **Reservation status:** PENDING, CONFIRMED, SEATED, COMPLETED, CANCELLED, NO_SHOW
- **Loyalty tier:** BRONZE, SILVER, GOLD, PLATINUM
- **Loyalty transaction type:** EARNED, REDEEMED, BONUS, EXPIRED
- **Delivery status:** PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, CANCELLED
- **Campaign type:** PERCENTAGE_DISCOUNT, FLAT_DISCOUNT, BUY_ONE_GET_ONE, FREE_DELIVERY, HAPPY_HOUR
- **Campaign status:** DRAFT, ACTIVE, PAUSED, EXPIRED, CANCELLED
