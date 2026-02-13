# Roadmap vs Implementation Checklist

This document cross-references **claude_code_roadmap.md** with the current codebase. Items are marked as **Done**, **Partial**, or **Not done** (optional / not implemented).

---

## Phase 1: Project Setup — **Done** (with structural differences)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| Project structure (frontend + backend + database) | Done | Backend is single Maven module (not 4 modules). Flyway migrations V1–V7. |
| Angular workspace, 4 apps, shared library | Done | |
| Spring Boot, JPA, Security, WebSocket, Validation, Mail | Done | |
| PostgreSQL, schema, seed data | Done | |

---

## Phase 2: Authentication — **Done**

| Roadmap item | Status |
|--------------|--------|
| JWT (generate, validate, filter) | Done |
| AuthController (login, register, refresh, me) | Done |
| AuthService, guards, interceptors | Done |
| Login / Register components | Done |

---

## Phase 3: Menu Management — **Done** (minor gaps)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| Menu APIs (categories, items, search, reorder, file upload) | Done | |
| Customer: menu list, tabs, search, filters | Done | Dietary, price range, allergens (exclude). |
| Customer: menu-filter component | Partial | Filters are inline in menu-list (chips + price). |
| Customer: menu-search component | Partial | Search is inline in menu-list. |
| Admin: menu-dashboard, category-list, category-form | Done | Category form has image upload (V8 + ImageUploadComponent). |
| Admin: item-list (table, image, pagination, sort, filter, duplicate) | Done | |
| Admin: item-form (image upload, dietary, allergens, station) | Done | Allergens (chips), Kitchen Station dropdown, dietary toggles. |
| Admin: item-details | Done | |
| Admin: ImageUploadComponent | Done | |
| Admin: Bulk upload | Done | CSV bulk upload + UI. |
| POST /upload/menu-image | Done | |

---

## Phase 4: Shopping Cart & Orders — **Done**

| Roadmap item | Status |
|--------------|--------|
| Order entities, DTOs, create/update/cancel | Done |
| Order APIs (paginated, customer, table/active, stats, items, cancel) | Done |
| Customer place order (POST /orders with CUSTOMER) | Done |
| CartService, cart view, checkout | Done |
| Floating cart icon with badge | Done |

---

## Phase 5: Kitchen Display — **Done** (optional extras not done)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| KitchenStation, orders by station | Done | Stations + GET /kitchen/stations, GET /kitchen/orders/station/{id}. |
| KitchenService (active orders, start, ready, item complete, stats) | Done | |
| Kitchen display (Kanban, order-card, timer) | Done | Timer in order-card; updates every second; color thresholds. |
| Station selector | Done | Dropdown in kitchen-display. |
| Order-timer component | Partial | Logic inline in order-card (no separate component). |
| Drag-and-drop between columns | Done | CDK drag-drop: New → Preparing → Ready; API called on drop. |
| Print order button | Done | Print button on order-card opens print window with order details. |

---

## Phase 6: Payment & Billing — **Done** (split options partial)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| Payment entity, process, refund, stats | Done | |
| Invoice entity, PDF, download | Done | |
| Email receipt (configurable) | Done | ReceiptEmailService, recipientEmail on payment. |
| GET /payments/daily-sales | Done | |
| Bill-view, payment-dialog, invoice-view | Done | |
| Split-bill dialog | Done | Equal split, by percentage, and custom (assign items to persons); backend SplitRequest supports all. |
| Quick bill / Settle from table view | Done | View Order / Bill → billing. |

---

## Phase 7: Admin Dashboard & Analytics — **Done** (some charts optional)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| Analytics APIs (dashboard, sales, menu, customers, staff, inventory, export) | Done | |
| Dashboard-home (stats, sales trend, category revenue, top items, orders by type/status) | Done | |
| Date range picker | Done | From/To + 7/14/30 + Apply. |
| Reports page (type, date, format, download) | Done | |
| Reusable components (stats-card, sales-chart, etc.) | Partial | All inline in dashboard-home; extraction is optional refactor. |
| Orders by Hour chart | Done | Bar chart using AdvancedAnalyticsService.getPeakHours(). |
| Payment Methods Distribution chart | Done | Donut chart; backend DashboardStats.ordersByPaymentMethod + PaymentRepository. |
| Recent Orders table | Done | Table with order #, status, amount, time; backend recentOrders + findTop10ByOrderByCreatedAtDesc. |
| Scheduled reports (email daily/weekly) | Not done | Documented in MONITORING.md as optional; not implemented. |

---

## Phase 8: Real-time & WebSocket — **Done**

| Roadmap item | Status |
|--------------|--------|
| STOMP, topics (kitchen, orders, tables) | Done |
| NotificationService, WebSocketService | Done |
| Kitchen/Waiter/Customer real-time updates | Done |

---

## Phase 9: Testing & Deployment — **Done** (E2E scope partial)

| Roadmap item | Status | Notes |
|--------------|--------|--------|
| Backend unit tests | Done | |
| Frontend Karma/Jasmine tests | Done | |
| Cypress E2E | Done | Specs for login, menu, cart; full “place order” flow would need backend. |
| Docker, docker-compose | Done | |
| CI (backend + frontend + Docker) | Done | |
| Security checklist, rate limiting | Done | SECURITY.md, RateLimitFilter. |
| Monitoring / Sentry | Partial | MONITORING.md; Sentry not integrated. |

---

## Additional Features (Optional) — **Done** where implemented

| Roadmap item | Status |
|--------------|--------|
| i18n, LanguageSelector | Done |
| Loyalty, staff, delivery, reviews, QR, campaigns | Done |
| Advanced analytics, recommendations | Done |
| Mobile (Ionic), marketing (email/SMS), AI | Not done | Backlog. |

---

## Summary: What’s left (optional or small)

1. **Admin item form**: Add **allergens** (text or chips) and **station** (dropdown from KitchenService.getStations()) so new/edit items can set them.
2. **Dashboard**: Optional charts: **Orders by Hour** (use advanced analytics peak-hours), **Payment Methods Distribution** (would need backend DTO + query), **Recent Orders** table.
3. **Dashboard**: Optional refactor: extract **stats-card**, **sales-chart**, **revenue-chart**, **top-items-chart**, **orders-timeline**, **quick-stats** into separate components.
4. **Kitchen**: Optional: **drag-and-drop** between Kanban columns, **Print order** button.
5. **Split bill**: Optional: **custom split** (assign items to persons), **by percentage** (backend + UI).
6. **Category form**: Optional: **category image** (would require schema + upload).
7. **E2E**: Optional: full **place order** Cypress flow with backend (or mocked API).

Nothing critical is missing for the roadmap’s core flow; the list above is optional or polish. See **Remaining gaps** below.

---

## Remaining gaps vs claude_code_roadmap.md

| Item | Roadmap expectation | Current state |
|------|---------------------|---------------|
| Admin menu routes | `items/new`, `items/:id/edit` as separate routes | Item form is dialog-based; no dedicated route for new/edit. |
| Customer menu | Separate menu-filter and menu-search components | Filter and search inline in menu-list. |
| Dashboard | Separate stats-card, sales-chart, revenue-chart, top-items-chart, orders-timeline, quick-stats | All inline in dashboard-home. |
| Kitchen | Separate order-timer component | Timer logic inline in order-card. |
| Kitchen | Full-screen toggle, dark mode, sound toggle, screen timeout, auto-scroll, estimated completion time | Not implemented. |
| Scheduled reports | Email daily/weekly reports | Documented in MONITORING.md; not implemented. |
| E2E | Full place-order flow (login to place order) | Cypress for login, menu, cart; full flow needs backend/mocks. |
| Sentry | Error tracking | MONITORING.md describes it; not integrated. |
| Backend structure | 4 Maven modules | Single Maven module. |
