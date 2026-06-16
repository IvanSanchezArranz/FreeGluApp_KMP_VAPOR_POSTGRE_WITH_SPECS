# Feature Specification: Production Bug Fixes (Koin & Postgres ID Alignment)

**Feature Branch**: `006-production-bug-fixes`
**Status**: Draft

## Vision & Product Overview
Resolve two critical production issues that block application operation:
1. **Android/KMP crash**: `KoinApplication has not been started` error when rendering screens that inject dependencies inside the Compose Multiplatform UI.
2. **Vapor/Postgres error**: `PostgresDecodingError` type mismatch when fetching products, caused by the database storing IDs as `BIGINT` (auto-incrementing integers) while the backend model `Food` expects them as `UUID`.

## Technical Architecture

### 1. Android/KMP Koin Context Integration
- Wrap the main application structure inside `KoinContext` at the root of `@Composable App()` in `App.kt`.
- This ensures that Koin's dependency injection context is correctly bound to the Compose Multiplatform lifecycle and CompositionLocal, resolving the `IllegalStateException`.

### 2. Vapor/Postgres ID Type Alignment
- Update `GlutenFreeAPI/Sources/GlutenFreeAPI/Models/Food.swift` to define the `@ID` field as `Int?` with `.database` generation:
  ```swift
  @ID(custom: .id, generatedBy: .database)
  var id: Int?
  ```
- Align the initializers in `Food.swift` to receive `id: Int? = nil`.
- Align unit tests in `GlutenFreeAPITests.swift` to use integer IDs instead of `UUID`.

### 3. KMP Client DTO Alignment
- Update `FoodDTO` in `DTOs.kt` to use `Int?` for the `id` field instead of `String?`.
- Update the mapper function `FoodDTO.toDomain()` in `Mappers.kt` to parse and map the ID using `this.id?.toString() ?: ""`. This keeps the domain model `FoodModel` fully decoupled and using `String`.

## Acceptance Criteria
- [ ] Koin starts successfully on Android and does not crash with `KoinApplication has not been started`.
- [ ] Vapor retrieves food records from PostgreSQL without throwing `PostgresDecodingError` for ID type mismatch.
- [ ] All GlutenFreeAPI unit tests compile and pass successfully.
- [ ] KMP shared library builds and runs correctly.
