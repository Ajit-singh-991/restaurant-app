# Roadmap Gaps – Implementation Status

This file summarizes what was implemented from the roadmap gaps plan and what remains optional.

## Implemented

### Backend
- **Menu**: PUT `/api/menu/categories/{id}`, PUT `/api/menu/categories/reorder`
- **Orders**: GET paginated, GET `/customer/{id}`, GET `/table/{tableId}/active`, GET `/stats`, PUT `/{id}/items`, POST `/{id}/cancel`; customer-initiated POST /orders (role-based)
- **File upload**: POST `/api/upload/menu-image`, `/images/**` static handler
- **Invoicing**: Invoice entity, PDF (OpenPDF), GET `/invoices/order/{orderId}`, GET `/invoices/{id}/download`, GET `/payments/daily-sales`
- **Analytics**: GET `/analytics/customers`, `/staff`, `/inventory` (stub), GET `/analytics/export?type=&format=&start=&end=`
- **Rate limiting**: In-memory per-IP (configurable), SECURITY.md checklist

### Frontend
- **Customer**: My orders (GET customer orders), floating cart FAB with badge, menu filters (dietary + **price range** min/max)
- **Admin menu**: Category list/form, drag-drop reorder, item list (dashboard table), item details route, ImageUploadComponent
- **Waiter billing**: Split-bill dialog, invoice view, **table grid “View Order / Bill”** → active table orders → billing; “Take Order” → orders list
- **Kitchen**: Order timer (per-second update, color thresholds) in order-card
- **Admin reports**: Reports page (type, date range, format), Generate & Download

### Phase 9
- Cypress E2E (customer + login specs), `npm run e2e` / `e2e:open`, CI job on main
- Rate limiting, SECURITY.md

## Optional – Now Implemented

- **Kitchen stations**: Entities, migration (V7), `GET /kitchen/stations`, `GET /kitchen/orders/station/{id}`; frontend station selector in kitchen-app.
- **Allergens**: `menu_items.allergens` (V7), MenuItem entity/DTO, admin form; customer menu “Exclude” filter (Nuts, Gluten, Dairy, Shellfish, Eggs).
- **Dashboard date range**: Start/End date inputs + Apply on dashboard-home; sales/charts use custom range.
- **Admin item list**: Image column, duplicate action, pagination (mat-paginator), sort (mat-sort), filter by category.
- **Email receipt**: Spring Mail; `app.mail.enabled`, `recipientEmail` on payment request; ReceiptEmailService sends PDF when enabled.
- **Split bill backend**: `POST /orders/{id}/split` with `numberOfWays`; waiter split-bill dialog uses API.
- **Bulk menu upload**: `POST /menu/admin/items/bulk` (CSV); admin “Bulk Upload CSV” button and result snackbar.
- **Scheduled reports / Sentry**: Documented in `MONITORING.md` (scheduled reports and Sentry are optional add-ons).
