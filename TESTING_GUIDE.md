# Testing Guide – Run All Apps

## Current status

- **Customer app** – Running at http://localhost:4200 (and open in Chrome).
- **Waiter app** – Running at http://localhost:4201 (and open in Chrome).
- **Admin app** – Running at http://localhost:4203 (and open in Chrome).
- **Kitchen app** – Running at http://localhost:4202. Open in Chrome: http://localhost:4202

## Docker (database + optional full stack)

Docker was not found in your PATH. To use Docker:

1. Install [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/) and start it.
2. Add Docker to your PATH or use the full path to `docker` / `docker-compose`.
3. From the project root run:
   ```powershell
   docker compose up -d --build
   ```
   This starts:
   - **PostgreSQL** on port 5432  
   - **Backend** on port 8080 (API at http://localhost:8080/api)  
   - **Frontend** on port 80 (customer at `/`, waiter at `/waiter`, kitchen at `/kitchen`, admin at `/admin`)

If you only want the database for local dev:

```powershell
docker compose up -d postgres
```

Then run the backend and frontend locally (see below).

## Backend (Spring Boot)

The backend was started but failed because it could not connect to PostgreSQL (wrong password or no database).

- **With Docker (only Postgres):**  
  Start Postgres with the step above. Default password in `docker-compose.yml` is `changeme_in_production`.  
  So either run the backend with:
  ```powershell
  $env:POSTGRES_PASSWORD="changeme_in_production"; cd backend; mvn spring-boot:run
  ```
  or set the same password in `application.yml` / env (e.g. `POSTGRES_PASSWORD=changeme_in_production`).

- **With full Docker stack:**  
  If you run `docker compose up -d`, the backend runs in Docker and you don’t need to run it locally.

- **Local run (after Postgres is up):**
  ```powershell
  cd backend
  mvn spring-boot:run
  ```
  API base: http://localhost:8080/api

## Frontend (Angular apps)

All four apps are running with increased Node memory to avoid “JavaScript heap out of memory” when running multiple dev servers.

| App       | URL                  | Script (from `frontend/`)     |
|----------|----------------------|-------------------------------|
| Customer | http://localhost:4200 | `npm run start:customer:safe` |
| Waiter   | http://localhost:4201 | `npm run start:waiter:safe`   |
| Kitchen  | http://localhost:4202 | `npm run start:kitchen:safe`  |
| Admin    | http://localhost:4203 | `npm run start:admin:safe`    |

Use the `:safe` scripts so each app gets a 4GB Node heap. Run each in its own terminal if you want all four at once.

## Test credentials (from CLAUDE.md)

Password for all seed users: **`password123`**

| Username  | Role    |
|-----------|---------|
| admin     | ADMIN   |
| manager1  | MANAGER |
| waiter1   | WAITER  |
| kitchen1  | KITCHEN |
| customer1 | CUSTOMER |

## Quick test in Chrome

1. **Customer:** http://localhost:4200 – menu, cart, login (customer1 / password123).
2. **Waiter:** http://localhost:4201 – tables, orders (waiter1 / password123).
3. **Kitchen:** http://localhost:4202 – kitchen display (kitchen1 / password123).
4. **Admin:** http://localhost:4203 – dashboard, menu, orders, reports (admin / password123).

For full testing (including API and database), start Docker (at least Postgres) and then the backend so the apps can load data and accept login.
