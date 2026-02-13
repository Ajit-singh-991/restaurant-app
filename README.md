# Restaurant Management System

A full-stack restaurant management system with **Angular 17** frontend and **Spring Boot 3.2** backend. It supports multi-role workflows: customer ordering, waiter table management, kitchen display, and admin dashboard with analytics.

## Features

- **Customer app** — Browse menu (with filters, search, allergens), cart, checkout, order tracking; i18n (EN/Hi/Es)
- **Waiter app** — Table grid, reservations, active orders, bill view, split bill (equal / percentage / custom), payment, invoice
- **Kitchen app** — Kanban (New → Preparing → Ready), drag-and-drop, station filter, per-item completion, print order, timers
- **Admin app** — Dashboard (stats, sales trend, category revenue, top items, orders by hour, payment methods, recent orders), menu management (categories & items with image/allergens/station), bulk CSV upload, reports export
- **Backend** — JWT auth, REST API, WebSocket (STOMP), Flyway migrations, loyalty, reviews, delivery, campaigns, advanced analytics, recommendations

## Architecture

```
restaurant-app/
├── frontend/                 # Angular 17 workspace (4 apps + shared library)
│   ├── projects/
│   │   ├── customer-app/     # Public ordering (port 4200)
│   │   ├── waiter-app/       # Waiter tablet (port 4201)
│   │   ├── kitchen-app/      # Kitchen display (port 4202)
│   │   ├── admin-app/        # Admin dashboard (port 4203)
│   │   └── shared/           # Models, services, guards, interceptors, shared components
│   └── package.json
├── backend/                  # Spring Boot 3.2 REST API (single Maven module)
│   ├── src/main/java/com/restaurant/
│   │   ├── config/           # Security, WebSocket, CORS, rate limit
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # JPA repositories
│   │   ├── service/          # Business logic
│   │   ├── security/         # JWT (UserPrincipal, filter, provider)
│   │   └── exception/        # GlobalExceptionHandler
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/     # Flyway (V1–V8)
│   └── pom.xml
├── docker-compose.yml        # PostgreSQL, backend, frontend (Nginx)
├── .github/workflows/ci.yml  # CI: backend + frontend + Docker build
├── CLAUDE.md                 # Developer/agent reference
└── ROADMAP_CHECKLIST.md      # Roadmap vs implementation status
```

## Tech Stack

| Layer    | Technology |
|----------|------------|
| Frontend | Angular 17, Angular Material, RxJS, ng2-charts, @ngx-translate, STOMP (WebSocket) |
| Backend  | Spring Boot 3.2.1, Spring Security, JPA, Flyway, Lombok, jjwt, Spring Mail |
| Database | PostgreSQL 15 |
| Auth     | JWT (Bearer), BCrypt |
| Real-time | WebSocket (STOMP over SockJS) at `/api/ws` |
| Build    | Maven (backend), Angular CLI (frontend) |
| Deploy   | Docker, Nginx (frontend), CI via GitHub Actions |

## Prerequisites

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 15+** (or use Docker)
- **Maven 3.9+**

## Quick Start

### 1. Database

```bash
docker-compose up -d postgres
```

Default DB: `restaurant_db`, user: `restaurant_admin`, password: `changeme` (override with env vars).

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

API: **http://localhost:8080/api** (context path is `/api`).

### 3. Frontend

```bash
cd frontend
npm install
```

Run any app:

```bash
npm run start:customer   # http://localhost:4200
npm run start:waiter     # http://localhost:4201
npm run start:kitchen    # http://localhost:4202
npm run start:admin      # http://localhost:4203
```

### 4. Full stack with Docker

```bash
docker-compose up --build
```

Frontend at **http://localhost** (Nginx: `/`, `/waiter`, `/kitchen`, `/admin`). API and WebSocket proxied to backend.

## Test Credentials

Use **username** (not email) and password **`password123`** for all seed users:

| Username   | Role     |
|------------|----------|
| admin      | ADMIN    |
| manager1   | MANAGER  |
| waiter1    | WAITER   |
| waiter2    | WAITER   |
| kitchen1   | KITCHEN  |
| customer1  | CUSTOMER |
| customer2–customer5 | CUSTOMER |

### Cannot sign in with these credentials?

A **Flyway migration (V9)** fixes the seed user passwords so they match `password123`. Restart the backend so migrations run:

1. Stop the backend if it is running.
2. Start it again: `cd backend && mvn spring-boot:run`.
3. After startup, sign in with any username from the table above and password **password123**.

If you use a fresh database and V9 has not been applied yet, the same migration will run on first backend start and fix the passwords. If login still fails, use the one-time reset: set `RESET_SEED_PASSWORDS=true`, start the backend once, then set it back to `false` (see `backend/README.md`).

## API Overview

| Prefix   | Description                    | Access |
|----------|--------------------------------|--------|
| `/auth`  | Login, register, me, refresh   | Public / Auth |
| `/menu`  | Categories, items, search, admin CRUD, bulk upload | Public / ADMIN |
| `/orders`| Create, list, status, cancel, split | Auth |
| `/kitchen` | Active orders, by station, start/ready, item complete, stats | KITCHEN, ADMIN |
| `/tables`  | List, create, delete          | Auth / ADMIN |
| `/reservations` | CRUD, status               | Auth |
| `/payments` | Process, list, refund, stats | Auth / ADMIN, MANAGER |
| `/analytics` | Dashboard, sales, menu, export | ADMIN, MANAGER |
| `/analytics/advanced` | Forecast, segments, peak hours | ADMIN, MANAGER |
| `/reviews`, `/loyalty`, `/staff`, `/qr`, `/delivery`, `/campaigns`, `/recommendations` | See CLAUDE.md |

WebSocket: **ws://localhost:8080/api/ws** — topics: `/topic/kitchen`, `/topic/orders`, `/topic/waiter`, `/topic/tables`.

## Testing

```bash
# Backend (JUnit 5 + Mockito, H2 in-memory)
cd backend && mvn test

# Frontend (Jasmine/Karma)
cd frontend && ng test

| Username   | Password      | Role     |
|------------|---------------|----------|
| admin      | password123   | ADMIN    |
| manager1   | password123   | MANAGER  |
| waiter1    | password123   | WAITER   |
| waiter2    | password123   | WAITER   |
| kitchen1   | password123   | KITCHEN  |
| customer1  | password123   | CUSTOMER |
# E2E (Cypress)
cd frontend && npm run e2e
## Documentation

- **CLAUDE.md** — Architecture, commands, API details, roles, DB migrations, patterns.
- **ROADMAP_CHECKLIST.md** — What’s implemented vs the step-by-step roadmap.
- **backend/README.md** — Backend setup, config, migrations, structure.
- **frontend/README.md** — Frontend apps, shared library, build, structure.
- **MONITORING.md** — Logging, optional Sentry, scheduled reports (if present).
- **SECURITY.md** — Security checklist, rate limiting (if present).

## License

Proprietary / as per your project.
