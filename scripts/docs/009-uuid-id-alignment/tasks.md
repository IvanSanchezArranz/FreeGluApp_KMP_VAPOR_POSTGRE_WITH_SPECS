# Tasks: UUID ID Type Alignment

- [x] T001: Create Spec 009 documentation files.
- [x] T002: Update Vapor model `Food.swift` with UUID-based ID and `@ID(key: .id)`.
- [x] T003: Update Vapor migration `CreateFood.swift` with standard `.id()` helper.
- [x] T004: Update `GlutenFreeAPITests.swift` to use `UUID` for invalid ID testing.
- [x] T005: Update KMP shared module DTO `FoodDTO` ID field to `String?`.
- [x] T006: Update KMP mapper `toDomain()` in `Mappers.kt` to map String ID directly.
- [x] T007: Run local Swift backend unit tests to verify database, model decoding, and routes.
- [x] T008: Compile KMP shared module to verify type safety and build success.
- [x] T009: Implement `SubcomposeAsyncImage` with loading/placeholder/error layouts in `FoodCard.kt`.
- [x] T010: Implement `SubcomposeAsyncImage` with loading/placeholder/error layouts in `FoodDetailScreen.kt`.
- [x] T011: Compile and verify that image loading states and fallbacks compile and work.
