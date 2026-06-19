# Feature Specification: Kotlin Ktor Backend Replication

**Feature Branch**: `010-kotlin-backend-replication`
**Status**: Approved

## Vision & Product Overview
Replicate the entire functionality of the Swift Vapor-based `GlutenFreeAPI` in a new Kotlin-based backend project (`GlutenFreeKtor`). This ensures a dual-backend architecture where both services are fully interchangeable for the KMP client. The KMP client should be able to switch its base URL to either backend target without changing any client-side business logic, UI, or serialization mapping.

---

## Technical Architecture

### 1. Architectural Alignment & Compatibility
- **Database Schema Parity**: Map Exposed ORM's database schema exactly to the existing PostgreSQL table `foods` managed by Fluent, including column names (`id` as UUID, `code`, `name`, `brand`, `categories`, `ingredients`, `image_url`, `countries`, `gluten_free`, `created_at`).
- **REST Endpoints Contract**: Expose identical REST endpoints matching Swift Vapor's routing scheme:
  - `GET /` -> Return plain text status message `API Gluten Free funcionando 🚀` with HTTP 200 OK.
  - `GET /foods` -> Return all foods paginated matching Fluent's Page schema layout.
  - `GET /foods/search?q={query}` -> Search foods by name, brand, or categories case-insensitively using `ILIKE` database bindings, paginated.
  - `GET /foods/{id}` -> Fetch a single food by standard UUID.
- **Serialization Formats**: Match camelCase JSON naming properties exactly with Vapor's output fields (`imageUrl`, `glutenFree`, `createdAt`) mapping them to snake_case column names.
- **CORS Setup**: Mirror the same allowed origins, methods, and headers config to prevent any Cross-Origin restrictions for Web Wasm targets.

### 2. Implementation Scope

#### Backend Framework (Ktor Server):
- **Routing & Controllers**: Standardize routing inside `FoodController.kt` to handle all catalog, search, and detail requests cleanly and securely.
- **Database Engine (Exposed + HikariCP)**: Initialize standard database connection pool using environment variables (`DATABASE_HOST`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `DATABASE_NAME`, `DATABASE_PORT`). Run database queries inside non-blocking `newSuspendedTransaction(Dispatchers.IO)` scopes to prevent blocking Ktor's Netty worker threads.
- **Serialization**: Configure standard Kotlinx Serialization with lenient and ignoreUnknownKeys configurations.

#### Containerization & DevOps:
- **Dockerfile**: Create a multi-stage secure container build using Gradle JDK21 builder stage and a JRE21 alpine runner stage. Create a dedicated non-root user `ktor` to run the start script.
- **Docker Compose**: Set up standard `docker-compose.yml` defining the Ktor service on port `8080` and its own isolated Postgres database container on port `5433` to prevent any local port collisions with Swift Vapor.

---

## Acceptance Criteria
- [ ] Kotlin Ktor backend builds and compiles successfully.
- [ ] Integration test suite runs 100% successfully on a clean H2 in-memory environment, covering success and negative routes (400 Bad Request, 404 Not Found, invalid UUID format).
- [ ] JSON response schemas and property names are identical to Swift Vapor's contracts.
- [ ] Pagination metadata follows the structure:
  ```json
  "metadata": { "page": Int, "per": Int, "total": Long }
  ```
- [ ] Database queries are bound securely to prevent any potential SQL injection.
- [ ] Server operates asynchronously using Kotlin Coroutines and non-blocking Netty engine.
