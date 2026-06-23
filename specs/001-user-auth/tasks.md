# Actionable Tasks: User Authentication and Login

This file contains the exact dependency-ordered checklists and verification commands to implement the User Authentication & Login feature.

---

## Phase 1: Database Schema & Core Models (Vapor)

### T101 AUTH-01 Crear migración y modelo para `User` en Swift Vapor
*   **Descripción**: Crear la migración `CreateUser` y el modelo Fluent `User.swift` en `GlutenFreeAPI/Sources/GlutenFreeAPI/Models/User.swift` con campos `id`, `email` (único, indexado) y `passwordHash`.
*   **Precondiciones**: Ninguna
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: Ninguna
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   La migración compila sin errores.
    *   La tabla se crea correctamente en PostgreSQL con sus índices.
*   **Test comando**: `cd GlutenFreeAPI && swift build`

### T102 AUTH-01 Crear migración y modelo para `UserFavorite` en Swift Vapor
*   **Descripción**: Crear la migración `CreateUserFavorite` y el modelo Fluent `UserFavorite.swift` en `GlutenFreeAPI/Sources/GlutenFreeAPI/Models/UserFavorite.swift` con relación `belongsTo` hacia `User` y `Food`.
*   **Precondiciones**: T101
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T101
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   La migración compila.
    *   Soporta borrado en cascada cuando el usuario es eliminado.
*   **Test comando**: `cd GlutenFreeAPI && swift build`

---

## Phase 2: Auth Controllers & JWT Integration (Vapor)

### T103 AUTH-02 Configurar JWT y Middleware de Autenticación en `configure.swift`
*   **Descripción**: Añadir soporte de firma JWT (utilizando clave secreta de variable de entorno `JWT_SECRET` con fallback seguro para dev) en `GlutenFreeAPI/Sources/GlutenFreeAPI/configure.swift`.
*   **Precondiciones**: T101
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T101
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Vapor compila con el paquete `JWT` integrado de manera nativa.
*   **Test comando**: `cd GlutenFreeAPI && swift build`

### T104 AUTH-02 Implementar `AuthController` con endpoints `/register` y `/login`
*   **Descripción**: Crear `AuthController.swift` en `GlutenFreeAPI/Sources/GlutenFreeAPI/Controllers/AuthController.swift` exponiendo las rutas POST `/register` y POST `/login` que devuelven el token JWT y los datos del usuario.
*   **Precondiciones**: T102, T103
*   **Estimación**: 5 SP / 3 dev-days
*   **Dependencias**: T102, T103
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Registrar un usuario hashea la contraseña de manera segura usando BCrypt.
    *   Login con credenciales válidas retorna el token firmado.
*   **Test comando**: `cd GlutenFreeAPI && swift run GlutenFreeAPI serve --port 8080` (probar con curl en otra terminal)

### T105 AUTH-02 Implementar endpoint protegido de sincronización de favoritos `/favorites/sync`
*   **Descripción**: Implementar ruta POST `/favorites/sync` protegida mediante el middleware JWT de Vapor, recibiendo una lista de UUIDs de alimentos y asociándolos a la cuenta del usuario autenticado.
*   **Precondiciones**: T104
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T104
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   Peticiones sin token o con token inválido reciben HTTP 401 Unauthorized.
    *   Peticiones válidas asocian los favoritos y devuelven HTTP 200 con el número de favoritos sincronizados.
*   **Test comando**: `cd GlutenFreeAPI && swift test`

---

## Phase 3: Client Token Storage & Repository (KMP)

### T106 AUTH-03 Implementar almacenamiento local de token `TokenStorage` en KMP
*   **Descripción**: Crear la clase de almacenamiento `TokenStorage` mediante expect/actual o shims directos en `FreeGluKMP/shared/src/commonMain/kotlin/com/ivan/freeglukmp/data/local/LocalFavoritesDataSource.kt` o similar para persistir el token de forma segura sin Room.
*   **Precondiciones**: Ninguna
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: Ninguna
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   El token se persiste correctamente entre reinicios de la aplicación.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T107 AUTH-03 Implementar `AuthRepository` e integrarlo en Koin
*   **Descripción**: Crear la interfaz `AuthRepository` y su implementación `AuthRepositoryImpl` conectando con los endpoints `/register` y `/login`. Configurar la inyección en `Koin.kt`.
*   **Precondiciones**: T106
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T106
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Se inyecta correctamente en el grafo de Koin.
    *   El cliente Ktor de red añade de forma automática la cabecera `Authorization: Bearer <token>` cuando el token está presente.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:allTests`

---

## Phase 4: UI Screen Implementations (Compose)

### T108 AUTH-04 Implementar pantallas de Login y Registro en Compose
*   **Descripción**: Crear `LoginScreen.kt` y `RegisterScreen.kt` en la capa de presentación de Compose, con campos de email/password, estados de carga y manejo visual de errores con Material 3.
*   **Precondiciones**: T107
*   **Estimación**: 5 SP / 3 dev-days
*   **Dependencias**: T107
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Manejo correcto de validación local de formularios (email correcto, password >= 8 chars).
    *   Navegación fluida entre pantallas.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T109 AUTH-04 Diálogo explícito de Fusión de Favoritos al iniciar sesión
*   **Descripción**: Crear diálogo modal interactivo en Compose que se dispare tras el login exitoso, preguntando al usuario si desea fusionar sus favoritos anónimos locales con su nueva cuenta en la nube.
*   **Precondiciones**: T108
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T108
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Si se confirma, llama al servicio `/favorites/sync` y limpia la base de datos de favoritos local.
    *   La lista de favoritos locales `localFavorites` se evalúa dinámicamente sin `remember` para evitar estados de caché obsoletos al reingresar a la pantalla.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

---

## Phase 5: Cloud Favorites Synchronization & Usecases Integration

### T110 AUTH-05 Implementar endpoints POST/DELETE unitarios para favoritos en Vapor
*   **Descripción**: Crear endpoints POST `/favorites/:foodID` y DELETE `/favorites/:foodID` en `AuthController.swift` protegidos por JWT para guardar/borrar favoritos individuales de la base de datos.
*   **Precondiciones**: T105
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T105
*   **Owner**: @owner-backend
*   **Acceptance Criteria**:
    *   POST de un favorito responde 201 Created y lo añade a Postgres.
    *   DELETE de un favorito responde 200 OK y lo borra de Postgres.
*   **Test comando**: `cd GlutenFreeAPI && swift test`

### T111 AUTH-05 Implementar cache de favoritos en memoria y endpoints POST/DELETE en KMP
*   **Descripción**: Actualizar `AuthRepository` y `AuthRepositoryImpl` para implementar funciones de guardado, borrado, cacheo de IDs de favoritos del servidor (con actualización optimista), y un inicializador de sincronización al hacer login o iniciar la app.
*   **Precondiciones**: T107
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T107
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Mantiene en memoria el cache de favoritos del servidor y ofrece consultas síncronas rápidas sin bloquear la UI.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`

### T112 AUTH-05 Alinear casos de uso de favoritos (`GetFavoriteFoodsUseCase`, `ToggleFavoriteUseCase`, `IsFavoriteUseCase`)
*   **Descripción**: Actualizar los tres casos de uso en `UseCases.kt` para desviar la lógica de forma transparente según el estado de autenticación de `AuthRepository.isLoggedIn()`.
*   **Precondiciones**: T111
*   **Estimación**: 3 SP / 2 dev-days
*   **Dependencias**: T111
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Si está autenticado, lee y escribe en la nube con actualización instantánea optimista del cache.
    *   Si es invitado, sigue leyendo y escribiendo únicamente en `LocalFavoritesDataSource`.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:allTests`

### T113 AUTH-05 Restaurar sesión de usuario de forma automática en el arranque
*   **Descripción**: Actualizar `App.kt` para comprobar si el usuario ya tiene sesión activa (`authRepository.isLoggedIn()`) al iniciar la aplicación. Si es así, configurar el estado inicial de la pantalla a `Screen.List` y pre-cargar el caché de favoritos en background mediante un `LaunchedEffect`.
*   **Precondiciones**: T112
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T112
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   Si el usuario tiene sesión activa, el catálogo de productos se muestra de forma inmediata al arrancar la app.
    *   Los favoritos del usuario se sincronizan y cachean de forma transparente en background al abrir el catálogo.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:allTests`

### T114 AUTH-05 Implementar clearAll() atómico para evitar condiciones de carrera en unlinks
*   **Descripción**: Añadir el método `clearAll()` a la declaración `expect` de `LocalFavoritesDataSource` y a sus implementaciones `actual` en Android, iOS, JS y Wasm. Actualizar `LoginScreen.kt` and `RegisterScreen.kt` para usar `clearAll()` de forma atómica en lugar de un bucle `forEach { toggleFavorite() }`.
*   **Precondiciones**: T113
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T113
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   La base de datos local de favoritos se limpia de forma atómica y completa en una sola transacción sin colisiones asíncronas.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:allTests`

### T115 AUTH-05 Configurar Info.plist y defaults.synchronize() en iOS
*   **Descripción**: Actualizar `Info.plist` del proyecto iOS para habilitar conexiones HTTP inseguras de desarrollo local, y agregar llamadas `.synchronize()` en las clases `actual` de iOS de almacenamiento local para persistencia instantánea.
*   **Precondiciones**: T114
*   **Estimación**: 2 SP / 1 dev-day
*   **Dependencias**: T114
*   **Owner**: @owner-frontend
*   **Acceptance Criteria**:
    *   La app en el simulador iOS puede conectarse exitosamente a `http://127.0.0.1:8080`.
    *   El token de autenticación y favoritos se persisten inmediatamente de forma física.
*   **Test comando**: `cd FreeGluKMP && ./gradlew :shared:assemble`




