# Feature Specification: UUID ID Type Alignment

**Feature Branch**: `009-uuid-id-alignment`
**Status**: Proposal

## Vision & Product Overview
Align the ID format of the Food model across all layers of FreeGluApp—from the PostgreSQL production database, the Swift Vapor backend models/migrations/endpoints, to the Kotlin Multiplatform (KMP) client. This resolves the `PostgresDecodingError` caused by a mismatch where the database schema expects a `uuid` (UUID) but the codebase tries to parse or decode it as an `int` (Int).

## Technical Architecture

### 1. Root Cause Analysis & Problem Context
- The real database table `foods` in the production PostgreSQL database (`glutenfree`) defines the `id` field as a `uuid` (UUID) with `uuid_generate_v4()` as the default value.
- In `Models/Food.swift`, the ID was defined as `@ID(custom: .id, generatedBy: .database) var id: Int?`.
- This mismatch caused a runtime crash whenever the client retrieved food details or lists, throwing:
  `invalid field: 'id', type: Optional<Int>, error: PostgresDecodingError`
- The KMP client likewise defined `id` as `Int?` in `FoodDTO` but mapped it to a `String` in `FoodModel`.

### 2. Implementation Scope

#### Backend (Swift Vapor):
- **`Models/Food.swift`**:
  - Update `id` type from `Int?` to `UUID?`.
  - Replace `@ID(custom: .id, generatedBy: .database)` with `@ID(key: .id)`.
  - Update `init` initializer signature and body to receive and set `id: UUID? = nil`.
- **`Migrations/CreateFood.swift`**:
  - Replace `.field(.id, .int, .identifier(auto: true))` with `.id()` to configure a UUID primary key.
- **`Tests/GlutenFreeAPITests.swift`**:
  - Replace the integer-based `let invalidId = 999999` with a valid random UUID: `let invalidId = UUID()`.

#### Frontend (Kotlin Multiplatform):
- **`DTOs.kt`**:
  - Update the `id` field of `FoodDTO` from `Int?` to `String?` to seamlessly decode the backend's UUID JSON strings.
- **`Mappers.kt`**:
  - Update the mapping from `FoodDTO.toDomain()` to set `id = this.id ?: ""` (removing the `.toString()` call on `Int`).

#### Image Placeholder Optimization (Coil 3):
- Use `SubcomposeAsyncImage` in `FoodCard.kt` and `FoodDetailScreen.kt`.
- Implement a graceful loading state showing a standard, circular/shimmer loading indicator when retrieving remote images.
- Implement an error/fallback state showing a stylized placeholder Card with a Material food/shopping-related icon (e.g. `Icons.Default.ShoppingCart` or `Icons.Default.BrokenImage`) for items with null/empty/invalid image URLs.

## Acceptance Criteria
- [ ] Swift Vapor backend builds and compiles successfully.
- [ ] Vapor backend unit tests run and pass without database connection or decoding errors.
- [ ] Kotlin Multiplatform (shared) compiles successfully.
- [ ] Food list and detail retrieval work end-to-end without any `PostgresDecodingError` or UUID mismatch exceptions.
- [ ] Items with empty, null, or failing image URLs display a beautiful fallback/placeholder icon instead of a blank space or crashing.
- [ ] Slow-loading images show a clean progress/loading indicator while loading.
