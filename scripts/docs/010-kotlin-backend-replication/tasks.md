# Implementation Tasks: Kotlin Ktor Backend Replication

This task list defines the exact milestones, sprints, and tasks needed to implement and verify the Kotlin Ktor backend replication project.

---

## Phase 1: Project Setup & Build Environment

### T101 KTOR-01 Initialize Kotlin Ktor Project Structure & Gradle Build Config
*   **Descripción**: Scaffold a new Kotlin Gradle project in the `GlutenFreeKtor/` folder. Add the necessary dependencies for Ktor (Server, Netty, ContentNegotiation, CORS, Logback), Exposed ORM, HikariCP, PostgreSQL Driver, Koin, and Kotlinx Serialization.
*   **Precondiciones**: None
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: None
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Gradle structure matches standard JVM layouts.
    *   `./gradlew assemble` compiles successfully.
    *   `settings.gradle.kts` specifies `rootProject.name = "GlutenFreeKtor"`.
*   **Test comando**: `cd GlutenFreeKtor && ./gradlew assemble`

### T102 KTOR-01 Add Dockerfile and docker-compose.yml Integration
*   **Descripción**: Create a `Dockerfile` for multi-stage JVM builds of the Ktor application, and update or provide a `docker-compose.yml` to support launching this container with a PostgreSQL database instance on port `8080` to prevent port collisions with the Swift Vapor service.
*   **Precondiciones**: T101
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T101
*   **Owner**: @owner-devops
*   **Acceptance Criteria**:
    *   Docker image compiles via `docker build -t glufree-ktor .`.
    *   Dockerfile uses secure non-root users (`useradd --system --create-home ...`).
    *   `docker-compose` can boot the server and the database cleanly.
*   **Test comando**: `cd GlutenFreeKtor && docker build -t glufree-ktor .`

---

## Phase 2: Database Layer & Schema Mapping

### T103 KTOR-02 Configure HikariCP and Connect Exposed ORM to PostgreSQL
*   **Descripción**: Implement `Database.kt` inside `com.glufree.ktor.configure`. Read database connection parameters from environment variables (`DATABASE_HOST`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `DATABASE_NAME`, `DATABASE_PORT`) with safe defaults. Initialize a HikariDataSource and connect Exposed's `Database.connect`.
*   **Precondiciones**: T101
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T101
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Application can connect to a local PostgreSQL instance.
    *   Errors are safely caught and logged if the database is unreachable.
*   **Test comando**: `cd GlutenFreeKtor && ./gradlew run` (with a local database active)

### T104 KTOR-02 Implement Exposed ORM Schema Mapping for Foods
*   **Descripción**: Implement `FoodsTable.kt` mapping exactly to the `foods` table managed by Fluent. Ensure all columns match Vapor's exact column types, names, and nullability, especially mapping `image_url` to `imageUrl`, `gluten_free` to `glutenFree`, and `created_at` to `createdAt` at the JSON level.
*   **Precondiciones**: T103
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T103
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Table declaration matches Fluent's structure.
    *   Columns map sychronously and without type safety warnings.
*   **Test comando**: `./gradlew test` using in-memory H2 or test-PostgreSQL environment.

---

## Phase 3: Routing & Route Implementations

### T105 KTOR-03 Implement Health and Base Routing Configuration
*   **Descripción**: Set up `Application.kt` with Routing feature and implement the root `GET /` endpoint to return the plain-text status message. Configure ContentNegotiation with Kotlinx Serialization and activate CORS middleware matching the allowed origin settings of the Vapor server.
*   **Precondiciones**: T101
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T101
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   `curl -i http://localhost:8080/` returns `HTTP 200 OK` and `API Gluten Free funcionando 🚀`.
    *   Response Headers include correct CORS policy permissions.
*   **Test comando**: `curl -sS http://localhost:8080/`

### T106 KTOR-03 Implement GET /foods Paginated Endpoint
*   **Descripción**: Create `FoodController.kt` and define the route `GET /foods`. Retrieve optional query parameters `page` and `per`. Query Exposed database to fetch a slice of foods (with `limit` and `offset`) and execute a total count query to build and return the `PageResponse` JSON structure.
*   **Precondiciones**: T104, T105
*   **Estimación**: 5 SP / 3 dev-days
*   **Dependencias**: T104, T105
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   `curl "http://localhost:8080/foods?page=1&per=5"` returns a JSON matching the Vapor Page structure:
        ```json
        {
          "items": [...],
          "metadata": { "page": 1, "per": 5, "total": 150 }
        }
        ```
*   **Test comando**: `curl -sS "http://localhost:8080/foods?page=1&per=5" | jq .`

### T107 KTOR-03 Implement GET /foods/search Case-Insensitive Paginated Endpoint
*   **Descripción**: Implement `GET /foods/search` within `FoodController.kt`. Extract search query parameter `q`. Perform a case-insensitive search matching the term against columns `name`, `categories`, and `brand` using Exposed's `lowerCase()` or custom SQL `ILIKE` operators. Paginate results identically to `GET /foods`.
*   **Precondiciones**: T106
*   **Estimación**: 5 SP / 3 dev-days
*   **Dependencias**: T106
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   `curl "http://localhost:8080/foods/search?q=almond"` returns foods containing case-insensitive "almond".
    *   Missing `q` query param returns HTTP 400 Bad Request.
*   **Test comando**: `curl -sS "http://localhost:8080/foods/search?q=almond" | jq .`

### T108 KTOR-03 Implement GET /foods/{id} Endpoint
*   **Descripción**: Implement route `GET /foods/{id}` to fetch a specific food product. Extract the `id` string, parse it as a JVM UUID, and search Exposed database. Return the full serialized Food response, or throw `404 Not Found` if missing.
*   **Precondiciones**: T106
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T106
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   `curl http://localhost:8080/foods/<existing-uuid>` returns the food object.
    *   Non-existent UUID returns HTTP 404 Not Found.
*   **Test comando**: `curl -sS -i http://localhost:8080/foods/99999999-9999-9999-9999-999999999999`

---

## Phase 4: Integration Testing & Parity Verification

### T109 KTOR-04 Add Ktor Server Tests and API Contract Verification Tests
*   **Descripción**: Implement integration tests inside `src/test/kotlin` using Ktor's `testApplication` engine. Mock or spin up a test PostgreSQL database, seed a clean set of foods, and test all routes (`GET /`, `GET /foods`, `GET /foods/search`, `GET /foods/{id}`) ensuring exact contract and pagination structure matches the Vapor server.
*   **Precondiciones**: T107, T108
*   **Estimación**: 5 SP / 3 dev-days
*   **Dependencias**: T107, T108
*   **Owner**: @owner-qa
*   **Acceptance Criteria**:
    *   `./gradlew test` passes 100% of integration checks.
    *   Test coverage reaches >= 80% for controller and routing modules.
*   **Test comando**: `cd GlutenFreeKtor && ./gradlew test`

---

## Phase 5: Client-Side Polish & UI Refinements

### T110 KMP-08 Fix FoodCard Grid Height Unevenness
*   **Descripción**: Fix the uneven height issue in the product list grid cards caused by titles having 1 line vs 2 lines of text. Set `minLines = 2` and `maxLines = 2` on the card's name Text component to guarantee that every card has a standardized, uniform height.
*   **Precondiciones**: None
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: None
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   `minLines = 2` and `maxLines = 2` configured on the card's name text.
    *   All cards in the LazyVerticalGrid have identical, standardized height, regardless of whether their title is 1 or 2 lines.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`
