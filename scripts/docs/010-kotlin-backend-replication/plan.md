# Implementation Plan: Kotlin Ktor Backend Replication

This plan outlines the specific steps required to build, configure, and verify the Kotlin Ktor-based parallel backend `GlutenFreeKtor`.

## 🛠️ Step-by-Step Implementation

### Step 1: Project Setup & Gradle Build Configuration
1.  **Create folder structure (`GlutenFreeKtor/`)**: Set up directories for Ktor main sources and resources, as well as test sources.
2.  **Create project configuration files**:
    *   `settings.gradle.kts`: Sets root project name to `"GlutenFreeKtor"`.
    *   `gradle.properties`: Sets JVM args and official code style.
    *   `build.gradle.kts`: Applies Kotlin JVM, Kotlinx Serialization, and application plugins. Configures dependencies for Ktor (Server, Netty, ContentNegotiation, CORS, Json), Exposed ORM, HikariCP, PostgreSQL Driver, Logback, and JUnit 5 testing tools with H2 database.
3.  **Bootstrap wrapper**: Copy standard Gradle wrapper files from `FreeGluKMP/` to provide a robust, self-contained local build toolchain.

### Step 2: Database Layer & Exposed ORM Schema Mapping
1.  **Configure connection pool (`configure/Database.kt`)**: Implement a HikariCP configuration that reads environment parameters safely (`DATABASE_HOST`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `DATABASE_NAME`, `DATABASE_PORT`) and establishes `Database.connect`.
2.  **Define Exposed Schema (`models/Food.kt` & `models/Pagination.kt`)**:
    *   Map `FoodsTable` to table `"foods"` with keys matching PostgreSQL/Fluent exact columns (`image_url` -> `imageUrl`, `gluten_free` -> `glutenFree`, `created_at` -> `createdAt`).
    *   Create a clean, serializable `FoodResponse` representation.
    *   Implement row mapper function `mapFoodRow(row)` to easily map database entries to serializable models.
    *   Create `PageResponse` and `PaginationMetadata` generic classes for Vapor-compatible pagination payloads.
    *   Create `ErrorResponse` serializable class to prevent polymorphic collection serialization crashes in Kotlinx.

### Step 3: Routing & Controller Endpoint Implementations
1.  **Configure CORS & Serialization (`configure/CORS.kt` & `configure/Serialization.kt`)**: Install ContentNegotiation with JSON format settings (lenient, ignoreUnknownKeys, encodeDefaults). Configure CORS with wildcards matching the access level of the original server.
2.  **Implement Route Handlers (`controllers/FoodController.kt`)**:
    *   `GET /`: Responds plain text `API Gluten Free funcionando 🚀`.
    *   `GET /foods`: Responds a paginated PageResponse of all products from the Exposed query under `newSuspendedTransaction(Dispatchers.IO)`.
    *   `GET /foods/search?q=`: Extracts `q` query, binds search terms securely using `FoodsTable.name.lowerCase() like searchPattern` (with % wildcard), and returns a paginated search payload.
    *   `GET /foods/{foodID}`: Validates UUID path parameter, query DB, and returns the food detail or throws `HttpStatusCode.NotFound` / `ErrorResponse`.
3.  **Register Routes (`configure/Routing.kt` & `Application.kt`)**: Bind all routes into routing context. Bootstrap Koin dependency injection in the entrypoint of the Netty server.

### Step 4: Containerization & DevOps Integration
1.  **Create `Dockerfile`**: Formulate a multi-stage compilation using `gradle:8.12.0-jdk21` as builder and `eclipse-temurin:21-jre-alpine` as running environment, running under secure non-root `ktor` user.
2.  **Create `docker-compose.yml`**: Provide a compose file spinning up the Ktor service on port `8080` and its Postgres DB container on port `5433` parallel to Vapor.

---

## 🧪 Verification Strategy

### 1. Integration Testing & Parity Verification (`ApplicationTest.kt`)
-   Implement comprehensive route testing using Ktor's `testApplication` engine inside `src/test/kotlin`.
-   Connect to a local H2 in-memory database during tests. To ensure complete test isolation, drop and recreate the `foods` table before each test run using Exposed's `SchemaUtils.drop(FoodsTable)` and `SchemaUtils.create(FoodsTable)` in a `@BeforeTest` hook.
-   Test all positive and negative scenarios (CORS headers, pagination total counts, query parameters, case-insensitive search, bad UUID format, and not-found records).

### 2. Execution Command
-   Run the complete test suite locally to verify success:
    ```bash
    cd GlutenFreeKtor && ./gradlew test
    ```
