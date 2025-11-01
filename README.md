# Bank REST API

A secure RESTful banking backend built with **Spring Boot 3.5**, **Spring Security JWT**, **PostgreSQL**, and **Liquibase**.  
Implements user and card management, transfers, and admin features, following the technical test requirements.

---

## Features

| Category | Highlights |
|-----------|-------------|
| **Security** | JWT authentication · Role-based access (USER / ADMIN) · Encrypted & masked PAN storage |
| **Domain** | Cards CRUD · Transfers between own cards · Block / Activate cards · Filtering + Pagination |
| **Admin** | Manage users and cards · Full audit access |
| **Persistence** | PostgreSQL + Liquibase migrations |
| **Documentation** | Swagger UI / OpenAPI 3 with JWT Authorize button |
| **Validation** | Bean Validation + centralized error handling |
| **Deployment** | Docker Compose for PostgreSQL dev environment |

---

## Setup & Run

### №1 Prerequisites
- JDK 17 +
- Docker Desktop or Docker Compose v2
- Maven 3.9 +

### №2 Clone repo and set env vars
```bash
git clone https://github.com/yourusername/Bank-REST-Api.git
cd Bank-REST-Api
```

The project uses `.env` for DB connection (example):

```env
DB_URL=jdbc:postgresql://localhost:5432/bank_db
DB_USER=postgres
DB_PASS=yourpassword
JWT_SECRET_B64=qD7ziVx+/xeythX9dM8OHGZnxI86t2Rq6Hvs6+w2N3c=
ATTRIBUTE_AES_KEY_B64=h8q7jZfF3e5eR3KM4gQ2rQ==
SERVER_PORT=8080
```

### №3 Start PostgreSQL via Docker
```bash
docker compose up -d
```
This launches a local `postgres:18` container with database `bank_db`.

### №4 Run the app
```bash
mvn spring-boot:run
```

The API runs at **http://localhost:8080**

---

## API Documentation

Swagger UI is generated automatically with **Springdoc 2.8.13**.

| Resource | URL |
|-----------|-----|
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| OpenAPI YAML | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

If you configured a custom UI path in `application.yml`:
```yaml
springdoc:
  swagger-ui:
    path: /docs
```
then open [http://localhost:8080/docs/index.html](http://localhost:8080/docs/index.html).

### Authorize with JWT
1. Obtain a token via `POST /auth/login`.
2. Click **Authorize** in Swagger UI.
3. Paste the token (without *Bearer *) and confirm.

---

## Database Migrations (Liquibase)

All migrations live under  
`src/main/resources/db/changelog/`.

Liquibase runs automatically on startup.  
To apply manually:

```bash
mvn liquibase:update
```

---

## Generate OpenAPI spec file

To produce the deliverable `docs/openapi.yaml`:

```bash
# App must be running
mvn -DskipTests=false verify
```

This uses the **springdoc-openapi-maven-plugin** to call `/v3/api-docs`  
and saves the YAML spec into `docs/openapi.yaml` (commit this file).

---

## Project Structure

```
src/
 ├─ main/java/com/example/bankcards/
 │   ├─ config/           # Security, JWT, OpenAPI config
 │   ├─ controller/       # Auth, User, Card controllers
 │   ├─ dto/              # DTOs for requests/responses
 │   ├─ entity/           # JPA entities
 │   ├─ repository/       # Spring Data repositories
 │   ├─ service/          # Business logic
 │   └─ exception/        # Global exception handling
 ├─ main/resources/
 │   ├─ db/changelog/     # Liquibase changelogs
 │   └─ application.yml   # Config (DB, JWT, Swagger)
 └─ test/java/...         # Unit tests (to be expanded)
```

---

## Endpoints Overview

| Role | Endpoint | Description |
|------|-----------|-------------|
| Public | `POST /auth/register` | Register user |
| Public | `POST /auth/login` | Authenticate & get JWT |
| USER | `GET /cards/me` | List own cards (paged) |
| USER | `POST /cards/me/create` | Create card |
| USER | `POST /cards/me/transfers` | Transfer between own cards |
| USER | `PATCH /cards/me/{id}/request-block` | Request card block |
| ADMIN | `GET /cards` | List all cards |
| ADMIN | `POST /cards/users/{userId}/create` | Create card for user |
| ADMIN | `PATCH /cards/{id}/block` | Block card |
| ADMIN | `PATCH /cards/{id}/activate` | Activate card |
| ADMIN | `DELETE /cards/{id}` | Delete card |
| ADMIN | `GET /users` | List users |
| ADMIN | `PUT /users/{id}` | Update user |
| ADMIN | `DELETE /users/{id}` | Delete user |

---

## Security Config Summary

```java
.requestMatchers(
  "/auth/**",
  "/v3/api-docs/**",
  "/v3/api-docs.yaml",
  "/swagger-ui.html",
  "/swagger-ui/**",
  "/docs/**"
).permitAll()
.anyRequest().authenticated();
```

The `JwtAuthenticationFilter` skips those whitelisted paths,  
while all others require a valid `Authorization: Bearer <token>` header.

---

## Docker Compose (Dev DB)

```yaml
version: '3.9'
services:
  db:
    image: postgres:18
    container_name: bank_db
    restart: always
    environment:
      POSTGRES_DB: bank_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: t4040657
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

---

## Troubleshooting

| Problem | Cause / Fix |
|----------|-------------|
| `{"reason":"No static resource docs"}` | Missing `springdoc-openapi-starter-webmvc-ui` or wrong path — fixed by installing **2.8.13** and permitting `/docs/**` |
| `/v3/api-docs` → 500 `ControllerAdviceBean.<init>` | Spring Boot 3.5 + old springdoc 2.6.x incompatibility → **upgrade to 2.8.13** |
| Swagger UI shows “Failed to load API definition” | Ensure `/v3/api-docs` returns 200 and isn’t blocked by JWT filter |
| UI 403 / 401 | Add `/docs/**` or `/swagger-ui/**` to security permitAll() and skip in JWT filter |

---

## Author

**Tikhon Ozhogin**  
_Test Assignment – Bank REST API_

---

### Status
**Working build** with Swagger UI + OpenAPI spec generation verified 
