# Implementation Plan: User Authentication and Login

**Branch**: `001-user-auth` | **Date**: 2026-06-23 | **Spec**: `specs/001-user-auth/spec.md`

## Summary

This plan outlines the design, backend implementation (Vapor & Ktor), shared KMP network/repository logic, and Compose UI views to deliver a secure, robust user authentication (JWT) system with anonymous favorites merging capabilities.

---

## Technical Context

*   **Language/Version**: Kotlin 2.0.x (KMP) / Swift 6 (Vapor) / Python 3 (ETL)
*   **Primary Dependencies**: 
    *   **Vapor**: `JWT` (Vapor JWT package), `Fluent`
    *   **Ktor**: `ktor-server-auth-jwt`, `bcrypt` (or JVM-native bcrypt library)
    *   **KMP**: `ktor-client-auth` (for Bearer authentication headers), `Koin 4.0.0`
*   **Storage**: PostgreSQL (`users` and `user_favorites` tables), local key-value stores (`SharedPreferences`/`NSUserDefaults`/`localStorage` via expect/actual)
*   **Testing**: XCTest (Vapor), JUnit (Ktor), KMP commonTests (`allTests`)

---

## Constitution Check

1.  **Koin Lifecycle**: Ensure `AuthRepository` is registered in `Koin.kt` and initialized síncronamente in native platform entries.
2.  **Adaptive Routing**: Any request to auth endpoints must use `getApiBaseUrl()` to safely resolve Android emulator (`10.0.2.2`) vs other platforms (`127.0.0.1`).
3.  **Local Storage**: Local JWT persistence must use expect/actual shims mapped to platform-native key-value APIs instead of Room.

---

## Project Structure

### Documentation

```text
specs/001-user-auth/
├── plan.md              # This file
├── spec.md              # Feature specification
└── tasks.md             # Actionable task list
```

### Source Code Modificacions

```text
GlutenFreeAPI/
└── Sources/GlutenFreeAPI/
    ├── Migrations/
    │   ├── CreateUser.swift
    │   └── CreateUserFavorite.swift
    ├── Models/
    │   ├── User.swift
    │   └── UserFavorite.swift
    ├── Controllers/
    │   └── AuthController.swift
    ├── configure.swift
    └── routes.swift

GlutenFreeKtor/
└── src/main/kotlin/com/glufree/ktor/
    ├── configure/
    │   └── Security.kt   # JWT setup
    ├── models/
    │   ├── User.kt
    │   └── UserFavorite.kt
    ├── controllers/
    │   └── AuthController.kt
    └── Application.kt

FreeGluKMP/
└── shared/src/commonMain/kotlin/com/ivan/freeglukmp/
    ├── data/
    │   ├── local/
    │   │   └── TokenStorage.kt   # Secure token storage
    │   └── remote/
    │       ├── AuthRepositoryImpl.kt
    │       └── ApiService.kt      # Bearer auth integration
    ├── domain/
    │   └── repository/
    │       └── AuthRepository.kt
    └── presentation/
        ├── auth/
        │   ├── LoginScreen.kt
        │   └── RegisterScreen.kt
        └── App.kt
```

---

## Phase 1: System Design

### Data Model

#### 1. Table `users`
*   `id`: UUID, Primary Key
*   `email`: String, Unique, Indexed
*   `password_hash`: String, BCrypt hashed
*   `created_at`: DateTime
*   `updated_at`: DateTime

#### 2. Table `user_favorites`
*   `id`: UUID, Primary Key
*   `user_id`: UUID, Foreign Key (users.id)
*   `food_id`: UUID, Foreign Key (foods.id)
*   `created_at`: DateTime

---

### Interface Contracts

#### 1. POST `/register`
*   **Request Body**:
    ```json
    {
      "email": "user@example.com",
      "password": "securepassword123"
    }
    ```
*   **Response Body (HTTP 201 Created)**:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com"
      }
    }
    ```

#### 2. POST `/login`
*   **Request Body**: Same as `/register`.
*   **Response Body (HTTP 200 OK)**: Same as `/register`.

#### 3. POST `/favorites/sync` (Protected endpoint)
*   **Headers**: `Authorization: Bearer <token>`
*   **Request Body**:
    ```json
    {
      "foodIds": [
        "a38a7c29-eeea-4c47-9f4a-939e1a8bb231",
        "08c62c95-3ca3-4e44-84d9-5f216511bcf7"
      ]
    }
    ```
*   **Response Body (HTTP 200 OK)**:
    ```json
    {
      "success": true,
      "syncedCount": 2
    }
    ```

#### 4. POST `/favorites/{foodID}` (Protected endpoint)
*   **Headers**: `Authorization: Bearer <token>`
*   **Response Body (HTTP 201 Created)**:
    ```json
    {
      "success": true,
      "message": "Favorite added"
    }
    ```

#### 5. DELETE `/favorites/{foodID}` (Protected endpoint)
*   **Headers**: `Authorization: Bearer <token>`
*   **Response Body (HTTP 200 OK)**:
    ```json
    {
      "success": true,
      "message": "Favorite removed"
    }
    ```

---

## Phase 2: Implementation Checklist

### 1. Swift Vapor Backend
- [ ] Add `CreateUser` and `CreateUserFavorite` migrations.
- [ ] Implement `User` and `UserFavorite` Fluent models.
- [ ] Implement `AuthController` with `/register` and `/login` routes.
- [ ] Set up JWT signing and verification middleware in `configure.swift`.
- [ ] Register `AuthController` in `routes.swift`.
- [ ] Implement POST `/favorites/{foodID}` and DELETE `/favorites/{foodID}` in `AuthController`.
- [ ] Write Vapor integration tests using `XCTVapor` / Swift `Testing`.

### 2. Kotlin Ktor Backend
- [ ] Implement JWT Auth configuration and route grouping in Ktor.
- [ ] Map `users` and `user_favorites` tables in Exposed ORM.
- [ ] Implement `AuthController` with `/register` and `/login`.
- [ ] Write integration tests in `ApplicationTest.kt`.

### 3. KMP Frontend
- [ ] Implement secure `TokenStorage` (expect/actual or SharedPreferences/NSUserDefaults wrappers).
- [ ] Create `AuthRepository` interface and its implementation `AuthRepositoryImpl` with local favorite IDs caching.
- [ ] Configure Ktor HTTP Client to automatically attach Bearer Auth header if token exists.
- [ ] Register auth classes in Koin module (`Koin.kt`).
- [ ] Implement POST/DELETE remote single-favorite calls in `AuthRepositoryImpl`.
- [ ] Add atomic `clearAll()` method to `LocalFavoritesDataSource` to prevent async write race conditions when unlinking.
- [ ] Configurar `defaults.synchronize()` en las implementaciones iOS de almacenamiento local para forzar persistencia inmediata.
- [ ] Configurar `NSAppTransportSecurity` en el `Info.plist` de la app iOS para permitir cargas HTTP locales a `127.0.0.1`.
- [ ] Update use cases `GetFavoriteFoodsUseCase`, `ToggleFavoriteUseCase`, and `IsFavoriteUseCase` to transparently switch between local data and `AuthRepository` depending on user login state.

### 4. Compose UI & Polish
- [ ] Create `LoginScreen.kt` with fields for Email/Password, Validation, Error Handling, and Guest mode.
- [ ] Create `RegisterScreen.kt` with validation and error flows.
- [ ] Integrate both screens into the Navigation flow in `App.kt`.
- [ ] Build the Favorites merge explicit modal when logging in with local unsynced favorites.
- [ ] Evitar el caching obsoleto mediante `remember` para la evaluación de `localFavorites` en `LoginScreen` y `RegisterScreen` de modo que siempre lea datos actualizados.
- [ ] Implement automatic session restoration in `App.kt` by checking login state on startup and navigating directly to `Screen.List`.
