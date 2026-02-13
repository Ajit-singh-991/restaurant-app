# Restaurant Management — Frontend

Angular **17** workspace with four applications and a shared library. All apps use **NgModules** (non-standalone), lazy-loaded feature modules, and **SCSS**.

## Applications

| App           | Port | Default route | Description                     | Roles        |
|---------------|------|---------------|---------------------------------|--------------|
| customer-app  | 4200 | /menu         | Menu browse, cart, checkout, orders, i18n | Public + CUSTOMER |
| waiter-app    | 4201 | /tables       | Tables, reservations, orders, billing, split bill, payment | WAITER, ADMIN |
| kitchen-app   | 4202 | /             | Kanban (New / Preparing / Ready), drag-and-drop, stations, print order | KITCHEN, ADMIN |
| admin-app     | 4203 | /dashboard    | Dashboard, analytics, menu management, categories, items, bulk upload, reports | ADMIN, MANAGER |

## Prerequisites

- **Node.js 18+**
- **npm** (or yarn)

## Setup

```bash
npm install
```

## Development

```bash
# Start one app at a time
npm run start:customer   # http://localhost:4200
npm run start:waiter     # http://localhost:4201
npm run start:kitchen    # http://localhost:4202
npm run start:admin      # http://localhost:4203
```

Each app has its own `environment.ts` with `apiUrl` and `wsUrl` (default: `http://localhost:8080/api` and `ws://localhost:8080/api/ws`). Path alias `@environments` resolves per project.

## Build

```bash
# Build all apps (production)
npm run build

# Build a single app
npm run build:customer
npm run build:waiter
npm run build:kitchen
npm run build:admin
```

Output under `dist/<app-name>/` (e.g. `dist/customer-app/`). For Docker/Nginx, see root `README.md` and `docker-compose.yml`.

## Testing

```bash
# Unit tests (Jasmine/Karma)
ng test

# Single project
ng test customer-app

# E2E (Cypress)
npm run e2e
npm run e2e:open
```

## Shared Library (`projects/shared`)

Import via path alias **`@shared`** (maps to `projects/shared/src/public-api.ts`):

```typescript
import { AuthService, MenuItem, OrderService, authGuard, roleGuard } from '@shared';
```

### Contents

- **Models** — User, MenuItem, Category, Order, OrderItem, CartItem, Table, Payment, DashboardStats, etc. (see `lib/models/`)
- **Services** — AuthService, CartService, MenuService, OrderService, TableService, KitchenService, AnalyticsService, AdvancedAnalyticsService, PaymentService, WebSocketService, ReviewService, LoyaltyService, StaffService, QrCodeService, DeliveryService, CampaignService, RecommendationService, UploadService
- **Guards** — authGuard, roleGuard
- **Interceptors** — jwtInterceptor, errorInterceptor
- **SharedModule** — LoginPageComponent, LanguageSelectorComponent, ImageUploadComponent
- **Components** — Reusable UI (e.g. ImageUploadComponent) and login/language selector

All public API is re-exported from `public-api.ts`; new shared code should be added there.

## Project Structure

```
projects/
├── customer-app/src/app/
│   ├── features/
│   │   ├── auth/           Login, register
│   │   ├── menu/           Menu list, filters, search, add to cart
│   │   ├── cart/           Cart view, checkout
│   │   └── orders/         Order history
│   └── app.module.ts
├── waiter-app/src/app/
│   └── features/
│       ├── auth/
│       ├── tables/         Table grid, reservations
│       ├── orders/         Active orders
│       └── billing/        Bill view, payment dialog, split bill, invoice
├── kitchen-app/src/app/
│   └── features/
│       ├── auth/
│       └── kitchen/        Kitchen display, order card (Kanban, drag-drop, print)
├── admin-app/src/app/
│   └── features/
│       ├── auth/
│       ├── dashboard/      Dashboard home (stats, charts, recent orders), reports
│       └── menu-management/ Menu dashboard, categories, items, category/form, item form, item details, bulk upload
└── shared/src/lib/
    ├── models/             TypeScript interfaces
    ├── services/           HTTP and WebSocket services
    ├── guards/             Auth and role guards
    ├── interceptors/      JWT and error interceptors
    └── components/         LoginPage, LanguageSelector, ImageUpload
```

## Tech Stack

- Angular 17, Angular Material, Angular CDK (e.g. drag-drop)
- RxJS, reactive forms
- Chart.js / ng2-charts (admin dashboard)
- @ngx-translate (customer-app i18n: en, hi, es)
- @stomp/rx-stomp, sockjs-client (WebSocket)

## Environment

Per-app `src/environments/environment.ts` and `environment.prod.ts`:

- `apiUrl` — REST API base (e.g. `http://localhost:8080/api`)
- `wsUrl` — WebSocket URL (e.g. `ws://localhost:8080/api/ws`)

Use `@environments` alias in code so each app keeps its own config.
