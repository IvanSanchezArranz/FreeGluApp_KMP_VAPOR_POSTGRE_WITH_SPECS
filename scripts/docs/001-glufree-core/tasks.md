
# Tasks: 001-glufree-core

Resumen: Desglose unitario y ordenado por dependencias para las feature-tasks solicitadas. Cada tarea incluye ID, descripción, precondiciones, estimación (SP / dev-days), dependencias por ID, owner placeholder, acceptance criteria y comandos de prueba local.

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 [P] KMP-00 Crear esqueleto Multiplatform (KMP) y archivo inicial libs.versions.toml en `buildSrc/libs.versions.toml` y `settings.gradle.kts`
  - Descripción: Crear estructura básica KMP (modules: shared, androidApp, iosApp, wasmApp) y copiar libs.versions.toml inicial en `buildSrc/`.
  - Precondiciones: Repositorio vacío para KMP; Java/Gradle instalados.
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: Ninguna
  - Owner: @owner-kmp
  - Acceptance Criteria: `./gradlew :shared:assemble` compila sin errores; `buildSrc/libs.versions.toml` existe y define versiones claves.
  - Test comando: ./gradlew :shared:assemble

- [ ] T002 [P] DEVOPS-01 Crear plantilla de workflows inicial (.github/workflows/build-artifacts.yml) para compilación de .wasm y .apk
  - Descripción: Añadir workflow que construya KMP shared, arme wasm artefact y apk en matrix básica; usar caching para Gradle/Kotlin.
  - Precondiciones: T001
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T001
  - Owner: @owner-devops
  - Acceptance Criteria: Workflow valid YAML y pasa validación `act` o `gh workflow lint` localmente; `./gradlew assemble` success en runner.
  - Test comando: ./gradlew assemble

- [ ] T003 [P] Configurar linters y formateadores (ktlint, detekt, SwiftLint, SwiftFormat) en repositorio (configs en `.github/` y root)
  - Descripción: Añadir configuración y tareas de verificación para CI (gradle ktlintCheck, detekt) y swiftlint script.
  - Precondiciones: T001
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T001
  - Owner: @owner-devops
  - Acceptance Criteria: `./gradlew ktlintCheck` y `./gradlew detekt` ejecutan sin error en CI; swiftlint script se puede ejecutar localmente.
  - Test comando: ./gradlew ktlintCheck || true

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T010 DB-01 Tarea principal: Crear contenedor PostgreSQL con Docker Compose en `infra/postgres/docker-compose.yml` y scripts de inicialización SQL en `infra/postgres/init/`
  - Descripción: Añadir docker-compose que exponga puerto, volumen persistente y usuario/DB por ENV; incluir script `00_schema.sql` placeholder.
  - Precondiciones: Docker instalado
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T001, T003
  - Owner: @owner-backend
  - Acceptance Criteria: `docker-compose -f infra/postgres/docker-compose.yml up -d` inicia Postgres; `psql` puede conectar con credenciales y `SELECT 1` responde.
  - Test comando: docker-compose -f infra/postgres/docker-compose.yml up -d && docker exec -it glufree-postgres psql -U glufree -c 'SELECT 1'

- [ ] T011 DB-01 Implementar pipeline ETL Python inicial en `etl/etl_main.py` que descarga OpenFoodFacts CSV/JSON y filtra registros con valor "gluten"/"gluten-free"
  - Descripción: Script que descarga dataset (o usa local file), filtra por tags/labels que indiquen gluten-free, normaliza campos relevantes y escribe salida parcial `etl/output/gluten_free.json`.
  - Precondiciones: T010 contenedor Postgres disponible (para ejecución final de carga). Python 3.11.
  - Estimación: 5 SP / 3 dev-days
  - Dependencias: T010
  - Owner: @owner-data
  - Acceptance Criteria: `python etl/etl_main.py --source openfoodfacts.json --out etl/output/gluten_free.json` genera JSON con >100 registros y cada objeto contiene `product_name` y `ingredients_text`.
  - Test comando: python3 etl/etl_main.py --sample tests/data/sample_off.json --out etl/output/gluten_free.json

- [ ] T012 DB-01 Crear Dockerfile para ETL y target `Makefile`/`pyproject` para ejecutar ETL y cargar a Postgres en `etl/Dockerfile` y `Makefile` (target: etl-load)
  - Descripción: Empaquetar el pipeline Python en contenedor que pueda conectar a Postgres y ejecutar inserts por lotes.
  - Precondiciones: T011, T010
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T011
  - Owner: @owner-data
  - Acceptance Criteria: `make etl-load` construye imagen `glufree-etl` y realiza carga de muestra en Postgres (verificación por SELECT COUNT(*) en tabla de productos).
  - Test comando: make etl-build && make etl-load

- [ ] T013 DB-02 Añadir `scripts/debug_csv.py` para exploración de integridad en `scripts/debug_csv.py` y tests básicos en `tests/etl/test_debug_csv.py`
  - Descripción: Script que valida columnas esperadas, tipos, y detecta registros incompletos; salida legible y exit code != 0 si falla cheques críticos.
  - Precondiciones: T011
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T011
  - Owner: @owner-data
  - Acceptance Criteria: `python scripts/debug_csv.py tests/data/sample_off.csv` devuelve exit 0 y reporta conteo de filas, columnas esperadas; si columna faltante devuelve exit 2.
  - Test comando: python3 scripts/debug_csv.py tests/data/sample_off.csv

- [ ] T014 DB-02 Programar cronjob semestral mediante GitHub Actions schedule (`.github/workflows/etl-schedule.yml`) que dispara `etl/etl_main.py` y crea PR con resultados
  - Descripción: GitHub Actions `schedule` con cron semiannually (e.g., `0 0 1 1,7 *`) que ejecuta ETL, valida output y almacena artifacts; include manual dispatch.
  - Precondiciones: T011, T012
  - Estimación: 1 SP / 0.5 dev-day
  - Dependencias: T011, T012
  - Owner: @owner-devops
  - Acceptance Criteria: Workflow disparable manualmente desde Actions tab; run exitoso genera artifact `gluten_free.json` y crea PR si difiere.
  - Test comando: gh workflow run etl-schedule.yml --ref main

- [ ] T015 API-01 Inicializar proyecto Vapor en `backend/vapor/` con `Package.swift` y estructura Sources/Tests
  - Descripción: `vapor new` o plantilla manual con soporte FluentPostgresDriver, XCTVapor test target.
  - Precondiciones: Swift toolchain instalado
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T010 (DB), T003
  - Owner: @owner-backend
  - Acceptance Criteria: `swift build` en `backend/vapor/` compila; `swift test` ejecuta (XCTVapor minimal).
  - Test comando: cd backend/vapor && swift build && swift test

- [ ] T016 API-01 Configurar Fluent con Postgres en `backend/vapor/Configuration/*` y deshabilitar TLS en local (env `TLS_DISABLE=true`)
  - Descripción: Conectar FluentPostgresDriver a la DB creada en T010; configuración env-based para dev vs prod.
  - Precondiciones: T010, T015
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T010, T015
  - Owner: @owner-backend
  - Acceptance Criteria: App levanta en local y se conecta a Postgres; `vapor run` con TLS_DISABLE=true inicia sin intentar TLS.
  - Test comando: TLS_DISABLE=true swift run Run

- [ ] T017 API-01 Implementar modelo Fluent `Food` y migración en `backend/vapor/Sources/App/Models/Food.swift` y `Migrations/` (schema minimal: id,name,ingredients,imageUrl,sourceUrl,lastUpdated,isCertified)
  - Descripción: Crear modelo y migración para almacenar productos filtrados por ETL.
  - Precondiciones: T016
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T016
  - Owner: @owner-backend
  - Acceptance Criteria: `swift run Run` aplica migración y tabla `foods` existe; `psql -c '\d foods'` muestra columnas esperadas.
  - Test comando: swift run Run migrate && docker exec -it glufree-postgres psql -U glufree -c '\d foods'

- [ ] T018 API-01 Implementar endpoint GET /foods paginado en `backend/vapor/Sources/App/Controllers/FoodController.swift`
  - Descripción: Endpoint que aplica query params `page` y `limit`, devuelve estructura {data,pagination} en JSON.
  - Precondiciones: T017
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T017
  - Owner: @owner-backend
  - Acceptance Criteria: `curl 'http://localhost:8080/foods?page=1&limit=20'` devuelve JSON con keys `data` y `pagination` y HTTP 200.
  - Test comando: curl -sS 'http://localhost:8080/foods?page=1&limit=20' | jq .

---

## Phase 3: API Endpoints & Auth (User stories mapped)

- [ ] T019 API-02 Implementar GET /foods/:id en `backend/vapor/Sources/App/Controllers/FoodController.swift`
  - Descripción: Recupera producto por UUID y devuelve full Product JSON.
  - Precondiciones: T017, T018
  - Estimación: 2 SP / 1 dev-day
  - Dependencias: T017, T018
  - Owner: @owner-backend
  - Acceptance Criteria: `curl http://localhost:8080/foods/<id>` devuelve HTTP 200 y objeto con `id,name,ingredients`.
  - Test comando: curl -sS http://localhost:8080/foods/<test-id> | jq .

- [ ] T020 API-02 Implementar GET /foods/search con query segura (ILIKE parametrizado) en `FoodController.swift`
  - Descripción: Endpoint `GET /foods/search?q=almond` usando SQL parameter binding para ILIKE; paginado.
  - Precondiciones: T017
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T017
  - Owner: @owner-backend
  - Acceptance Criteria: `curl 'http://localhost:8080/foods/search?q=almond'` devuelve products cuyo `name` contiene case-insensitive `almond`; no concatenated SQL in codebase.
  - Test comando: curl -sS 'http://localhost:8080/foods/search?q=almond' | jq .

- [ ] T021 API-03 Crear migración Fluent para `users` y `user_favorites` en `Migrations/` y modelos `User.swift`, `UserFavorite.swift`
  - Descripción: Crear tablas con índices necesarios (email unique, FK to foods) y timestamps.
  - Precondiciones: T017
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T017
  - Owner: @owner-backend
  - Acceptance Criteria: Migración crea `users` y `user_favorites`; constraints aplicados (unique email, FK integrity).
  - Test comando: swift run Run migrate && docker exec -it glufree-postgres psql -U glufree -c '\d users'

- [ ] T022 API-03 Implementar `AuthController` con endpoints `/register` y `/login` que devuelven JWT en `backend/vapor/Sources/App/Controllers/AuthController.swift`
  - Descripción: Registro con email/password (hashed), login con JWT (access + refresh optionally); tokens firmados con env secret.
  - Precondiciones: T021
  - Estimación: 5 SP / 3 dev-days
  - Dependencias: T021
  - Owner: @owner-backend
  - Acceptance Criteria: `curl -X POST http://localhost:8080/register -d '{"email":"a@b.com","password":"pw"}'` devuelve 201 y JSON `{ "token": "..." }`; `curl` to /login returns token.
  - Test comando: curl -sS -X POST http://localhost:8080/register -H 'Content-Type: application/json' -d '{"email":"test@example.com","password":"Pass1234"}' | jq .

- [ ] T023 API-03 Añadir pruebas XCTVapor para endpoints auth y foods en `backend/vapor/Tests/AppTests/`
  - Descripción: Tests de integración que arrancan app en memoria y prueban /register,/login,/foods
  - Precondiciones: T018, T022
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T018, T022
  - Owner: @owner-backend
  - Acceptance Criteria: `swift test` pasa tests que cubren auth + foods endpoints (CI target ≥80% coverage para módulos core en Phase 1 scope)
  - Test comando: cd backend/vapor && swift test

---

## Phase 4: KMP Client Features

- [ ] T030 KMP-01 Implementar inyección de dependencias con Koin en `shared/src/commonMain/kotlin/di/` y resolver colisiones expect/actual para iOS/Wasm
  - Descripción: Añadir Koin modules, crear expect/actual shims para platform-specific singletons y documentar resolución de colisiones en `shared/README.md`
  - Precondiciones: T001
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T001
  - Owner: @owner-kmp
  - Acceptance Criteria: `./gradlew :shared:assemble` compila; DI module puede resolver ViewModel en Android and iOS app targets.
  - Test comando: ./gradlew :shared:test

- [ ] T031 KMP-02 Crear Data, Domain, DTOs en `shared/src/commonMain/kotlin/com/glufree/` y FoodsPagingSource que mapea paginación JSON correctamente
  - Descripción: Crear modelos (ProductDto, Product), mappers y PagingSource usando Ktor/Kotlinx Serialization que consuma `/foods` paginado.
  - Precondiciones: T018, T020
  - Estimación: 5 SP / 3 dev-days
  - Dependencias: T018, T020
  - Owner: @owner-kmp
  - Acceptance Criteria: MockEngine unit test devuelve paginated list correctamente mapeada en `FoodsPagingSource` con nextPage detection.
  - Test comando: ./gradlew :shared:test --tests *FoodsPagingSource*

- [ ] T032 KMP-03 Maquetar `FoodsListScreen` y `FoodDetailScreen` en Compose en `shared/ui` con navegación MVVM
  - Descripción: Implementar pantallas Compose multiplatform y Navigation (NavHost), ViewModels en shared/viewmodel
  - Precondiciones: T031
  - Estimación: 5 SP / 3 dev-days
  - Dependencias: T031
  - Owner: @owner-frontend
  - Acceptance Criteria: `./gradlew :androidApp:assembleDebug` instala en emulator y muestra list screen with mock data; navigation to detail works.
  - Test comando: ./gradlew :androidApp:installDebug

- [ ] T033 KMP-04 Integrar Room (Android) / SQLDelight (common) e implementar `ToggleFavoriteUseCase` con encolamiento de sincronización hacia POST /sync/favorites en `shared/data/local` y `androidApp/` WorkManager
  - Descripción: Local DB para favorites, caso de uso toggle que actualiza local y encola tarea de sync. Endpoint objetivo: backend POST /sync/favorites.
  - Precondiciones: T021 (user_favorites schema), T022 (auth)
  - Estimación: 8 SP / 5 dev-days
  - Dependencias: T021, T022, T031
  - Owner: @owner-kmp
  - Acceptance Criteria: ToggleFavorite persiste en DB y encola trabajo; cuando backend disponible, worker envía POST /sync/favorites y marca `synced`.
  - Test comando: ./gradlew :androidApp:connectedAndroidTest && curl -X POST http://localhost:8080/sync/favorites -H 'Authorization: Bearer <token>' -d @tests/data/sync_sample.json

- [ ] T034 KMP-05 Integrar debounce y FilterChip para búsqueda en tiempo real en `shared/ui/components/SearchBar.kt` y `shared/ui/components/FilterChip.kt`
  - Descripción: Debounce 500ms en ViewModel search input; FilterChip UI control que aplica AND filters.
  - Precondiciones: T031, T032
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T031, T032
  - Owner: @owner-frontend
  - Acceptance Criteria: Al tipear en SearchBar, no se hacen llamadas hasta 500ms; chips aplican filtros de forma inmediata (no debounce). Verificable en UI con Mock network.
  - Test comando: ./gradlew :shared:test --tests *SearchDebounce*

- [ ] T035 FEAT-AUTH Implementar autenticación end-to-end en cliente (domain entities, DTOs, ViewModel, DI) y almacenamiento seguro de tokens en iOS Keychain / Android EncryptedSharedPreferences
  - Descripción: Implementar flow register/login, token refresh, storage, and auth-aware API client in `shared/network`.
  - Precondiciones: T022, T030
  - Estimación: 8 SP / 5 dev-days
  - Dependencias: T022, T030
  - Owner: @owner-kmp
  - Acceptance Criteria: Cliente puede login/register against local backend and receives token; subsequent API requests include Bearer header; tokens persisted securely across app restarts.
  - Test comando: ./gradlew :shared:test --tests *Auth* && curl -H "Authorization: Bearer <token>" http://localhost:8080/foods

---

## Phase 5: UI, QA & Polish

- [ ] T040 UI-01 Definir tipografías y colores Material3 y componentes `GlutenFreeButton` / `FoodCard` en `shared/ui/theme/` y `shared/ui/components/`
  - Descripción: Theme.kt con colorScheme, typography, and reusable components; accessible contrast ratios.
  - Precondiciones: T032
  - Estimación: 3 SP / 2 dev-days
  - Dependencias: T032
  - Owner: @owner-design
  - Acceptance Criteria: Theme compiles and components render in UI; contrast ratios >= WCAG requirements (verify with color tools).
  - Test comando: ./gradlew :androidApp:installDebug && run accessibility scanner

- [ ] T041 QA-01 Escribir tests de integración XCTVapor (`backend/vapor/Tests/`) y tests unitarios KMP para `FoodRepository` usando MockEngine (`shared/src/commonTest/kotlin`)
  - Descripción: Cobertura de tests integración backend y repository unit tests con MockEngine para alcanzar objetivo mínimo 80% en módulos core.
  - Precondiciones: T018, T023, T031
  - Estimación: 8 SP / 5 dev-days
  - Dependencias: T018, T023, T031
  - Owner: @owner-qa
  - Acceptance Criteria: `swift test` e `./gradlew :shared:test` pasan; cobertura reportada >= 80% en target modules.
  - Test comando: cd backend/vapor && swift test; ./gradlew :shared:jacocoTestReport

- [ ] T042 DEVOPS-01 Completar `deploy-backend.yml` para despliegue a staging y workflows para compilar wasm y apk en `.github/workflows/deploy-backend.yml` y `.github/workflows/build-artifacts.yml`
  - Descripción: Workflow de despliegue que construye backend y publica imagen Docker a registry, y workflows de artefactos para wasm .wasm y Android .apk builds.
  - Precondiciones: T002, T015
  - Estimación: 5 SP / 3 dev-days
  - Dependencias: T002, T015
  - Owner: @owner-devops
  - Acceptance Criteria: On push to staging, workflow builds and pushes docker image; build-artifacts produces wasm and apk artifacts downloadable from Actions.
  - Test comando: gh workflow run deploy-backend.yml --ref main

---

## Dependencies & Execution Order (resumen)

- Foundation: T001 → T003 → T010 → T015 → T016 → T017 → T018
- ETL flow: T011 → T012 → T014
- Auth & Users: T021 → T022 → T023 → T035
- KMP client: T030 → T031 → T032 → T033 → T034 → T035
- QA & CI: T041 depends on API and KMP units (T018,T023,T031)

## Sprint 1 - Prioridad (MVP slice)

Sprint 1 recommended tasks (minimum to demo a working catalog + search + local favorites demo with mock data):
- T001, T003, T010, T015, T016, T017, T018, T011 (ETL MVP with sample), T031 (DTOs/Paging), T032 (UI mocks), T041 (initial tests)

## Notas finales

- Todas las tareas siguen TDD y linters según constitución; objetivo cobertura mínima 80% en módulos core (pagination, search, sync).
- Los `Owner` son placeholders; asignar antes de crear issues.
